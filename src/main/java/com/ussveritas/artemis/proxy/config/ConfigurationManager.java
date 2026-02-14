package com.ussveritas.artemis.proxy.config;
import java.util.concurrent.locks.*;
public class ConfigurationManager{private final ReadWriteLock lock=new ReentrantReadWriteLock();private volatile ProxyConfiguration config;public ConfigurationManager(){this.config=ProxyConfiguration.createDefault();}public ProxyConfiguration get(){lock.readLock().lock();try{return config;}finally{lock.readLock().unlock();}}public void update(ProxyConfiguration newConfig){lock.writeLock().lock();try{this.config=newConfig;}finally{lock.writeLock().unlock();}}}
