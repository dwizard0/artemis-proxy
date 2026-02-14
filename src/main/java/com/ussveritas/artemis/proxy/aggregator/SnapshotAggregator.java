package com.ussveritas.artemis.proxy.aggregator;

import com.ussveritas.artemis.proxy.model.*;
import com.ussveritas.artemis.proxy.observer.*;
import com.ussveritas.artemis.proxy.model.GameObjectInfo;
import com.walkertribe.ian.world.*;
import com.walkertribe.ian.enums.*;
import com.walkertribe.ian.util.*;
import org.slf4j.*;

import java.util.*;
import java.util.concurrent.locks.*;

public class SnapshotAggregator {
    private static final Logger log = LoggerFactory.getLogger(SnapshotAggregator.class);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    private final Map<GhostType, World> worlds = new EnumMap<>(GhostType.class);
    private volatile ShipSnapshot currentSnapshot;
    private volatile int targetShip = 0;
    
    public SnapshotAggregator() {
        this.currentSnapshot = ShipSnapshot.createEmpty();
        for (GhostType type : GhostType.values()) {
            worlds.put(type, new World());
        }
    }
    
    public World getWorldFor(GhostType ghost) {
        return worlds.get(ghost);
    }
    
    public void setTargetShip(int ship) {
        this.targetShip = ship;
    }
    
    public void updateSnapshot() {
        lock.writeLock().lock();
        try {
            currentSnapshot = buildSnapshot(targetShip);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    private ShipSnapshot buildSnapshot(int shipNumber) {
        ArtemisPlayer player = null;
        
        for (World world : worlds.values()) {
            player = world.getPlayer((byte) shipNumber);
            if (player != null) break;
        }
        
        if (player == null) {
            return ShipSnapshot.createEmpty();
        }
        
        return new ShipSnapshot(
            buildIdentity(player, shipNumber),
            buildPosition(player),
            buildStatus(player),
            buildShields(player),
            buildWeapons(player),
            buildSystems(player),
            buildTactical(player)
        );
    }
    
    private ShipIdentity buildIdentity(ArtemisPlayer player, int shipNumber) {
        String name = player.getName() != null ? player.getName().toString() : null;
        Integer side = (int) player.getSide();
        
        return new ShipIdentity(name, null, side, shipNumber);
    }
    
    private ShipPosition buildPosition(ArtemisPlayer player) {
        return new ShipPosition(
            player.getX(),
            player.getY(),
            player.getZ(),
            player.getHeading(),
            player.getVelocity(),
            null, // pitch not directly available
            null, // roll not directly available
            (int) player.getWarp(),
            player.getImpulse()
        );
    }
    
    private ShipStatus buildStatus(ArtemisPlayer player) {
        BoolState shieldsState = player.getShieldsState();
        Float frontShields = player.getShieldsFront();
        Boolean shieldsUp = BoolState.safeValue(shieldsState);
        
        AlertStatus alertStatus = player.getAlertStatus();
        Boolean redAlert = alertStatus != null && alertStatus == AlertStatus.RED;
        
        Integer dockingBase = player.getDockingBase();
        Boolean docked = dockingBase != null && dockingBase != 0;
        
        return new ShipStatus(
            player.getShieldsFront(), // hull uses shields front as proxy
            5500.0f, // hull max - hardcoded for now
            shieldsUp,
            redAlert,
            docked
        );
    }
    
    private ShieldStatus buildShields(ArtemisPlayer player) {
        return new ShieldStatus(
            player.getShieldsFront(),
            5500.0f, // max shields - hardcoded for now
            player.getShieldsRear(),
            5500.0f,
            null, // port shields not in IAN 3.5.1
            null,
            null, // starboard shields not in IAN 3.5.1
            null
        );
    }
    
    private WeaponStatus buildWeapons(ArtemisPlayer player) {
        List<TubeData> tubes = new ArrayList<>();
        
        for (int i = 0; i < 6; i++) {
            TubeState state = player.getTubeState(i);
            OrdnanceType contents = player.getTubeContents(i);
            float countdown = player.getTubeCountdown(i);
            
            tubes.add(new TubeData(
                i,
                state == TubeState.LOADED,
                contents != null ? contents.name() : null,
                Float.isNaN(countdown) ? null : countdown
            ));
        }
        
        com.walkertribe.ian.enums.BeamFrequency freq = player.getBeamFrequency();
        
        TorpedoCounts torpedoCounts = new TorpedoCounts(
            player.getTorpedoCount(OrdnanceType.TORPEDO),
            player.getTorpedoCount(OrdnanceType.NUKE),
            player.getTorpedoCount(OrdnanceType.MINE),
            player.getTorpedoCount(OrdnanceType.EMP),
            player.getTorpedoCount(OrdnanceType.PSHOCK),
            null, // BEACON not ordnance type
            player.getTorpedoCount(OrdnanceType.PROBE),
            null  // TAG not ordnance type
        );
        
        TargetingMode targetingMode = player.getTargetingMode();
        Boolean autoBeams = targetingMode != null && targetingMode == TargetingMode.AUTO;
        
        return new WeaponStatus(
            tubes,
            6, // tube count
            torpedoCounts,
            new com.ussveritas.artemis.proxy.model.BeamFrequency(freq != null ? freq.ordinal() : null),
            autoBeams
        );
    }
    
    private ShipSystems buildSystems(ArtemisPlayer player) {
        Float energy = player.getEnergy();
        Integer totalCoolant = getTotalCoolant(player);
        
        ReactorStatus reactor = new ReactorStatus(energy, 1000.0f, totalCoolant);
        
        return new ShipSystems(
            reactor,
            buildSystemStatus(player, ShipSystem.BEAMS),
            buildSystemStatus(player, ShipSystem.TORPEDOES),
            buildSystemStatus(player, ShipSystem.SENSORS),
            buildSystemStatus(player, ShipSystem.MANEUVERING),
            buildSystemStatus(player, ShipSystem.IMPULSE),
            buildSystemStatus(player, ShipSystem.WARP_JUMP_DRIVE),
            buildSystemStatus(player, ShipSystem.FORE_SHIELDS),
            buildSystemStatus(player, ShipSystem.AFT_SHIELDS),
            null, // port shields system
            null  // starboard shields system
        );
    }
    
    private SystemStatus buildSystemStatus(ArtemisPlayer player, ShipSystem system) {
        return new SystemStatus(
            player.getSystemEnergy(system),
            player.getSystemHeat(system),
            player.getSystemCoolant(system),
            0.0f // damage not available in IAN 3.5.1 - always 0.0
        );
    }
    
    private Integer getTotalCoolant(ArtemisPlayer player) {
        int total = 0;
        for (ShipSystem system : ShipSystem.values()) {
            Integer coolant = player.getSystemCoolant(system);
            if (coolant != null) {
                total += coolant;
            }
        }
        return 8 - total; // 8 total coolant minus allocated
    }
    
    private TacticalStatus buildTactical(ArtemisPlayer player) {
        return new TacticalStatus(
            null, // main screen view not available
            player.getWeaponsTarget(),
            player.getScienceTarget(),
            player.getScanProgress()
        );
    }
    
    public ShipSnapshot getCurrentSnapshot() {
        lock.readLock().lock();
        try {
            return currentSnapshot;
        } finally {
            lock.readLock().unlock();
        }
    
    }
    public List<GameObjectInfo> getVisibleObjects() {
        List<GameObjectInfo> objects = new ArrayList<>();
        for (World world : worlds.values()) {
            for (ArtemisObject obj : world) {
                if (obj == null) continue;
                GameObjectInfo info = extractObjectInfo(obj);
                if (info != null) {
                    objects.add(info);
                }
            }
        }
        return objects;
    }
    private GameObjectInfo extractObjectInfo(ArtemisObject obj) {
        Integer id = obj.getId();
        String name = obj.getName() != null ? obj.getName().toString() : null;
        String type = obj.getType() != null ? obj.getType().name() : null;
        Float x = obj.getX();
        Float y = obj.getY();
        Float z = obj.getZ();
        Float shields = null;
        Float shieldsMax = null;
        Float bearing = null;
        Float velocity = null;
        Integer side = null;
        if (obj instanceof BaseArtemisShip) {
            BaseArtemisShip ship = (BaseArtemisShip) obj;
            bearing = ship.getHeading();
            velocity = ship.getVelocity();
            side = (int) ship.getSide();
            shields = ship.getShieldsFront();
        }
        return new GameObjectInfo(id, name, type, side, x, y, z, shields, shieldsMax, bearing, velocity);
    }
}
