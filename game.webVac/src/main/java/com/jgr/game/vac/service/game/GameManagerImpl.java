package com.jgr.game.vac.service.game;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.jgr.game.vac.dao.GamePropsDao;
import com.jgr.game.vac.dao.GameSetupDao;
import com.jgr.game.vac.dao.PropertyDao;
import com.jgr.game.vac.entity.GameProps;
import com.jgr.game.vac.entity.GameSetup;
import com.jgr.game.vac.entity.Property;
import com.jgr.game.vac.service.AbstractGameObject;
import com.jgr.game.vac.service.AbstractGameOperation;
import com.jgr.game.vac.service.AbstractGamePoller;
import com.jgr.game.vac.service.AbstractGameRunnable;
import com.jgr.game.vac.service.AbstractGameState;
import com.jgr.game.vac.service.AbstractGameTestValue;
import com.jgr.game.vac.service.AbstractGameTimedRunnable;
import com.jgr.game.vac.service.GameManager;
import com.jgr.game.vac.service.GameOperation;
import com.jgr.game.vac.service.GamePoller;
import com.jgr.game.vac.service.GameRunnable;
import com.jgr.game.vac.service.GameStatus;
import com.jgr.game.vac.service.GameTestValue;
import com.jgr.game.vac.service.GameThread;
import com.jgr.game.vac.service.GameTimedRunnable;
import com.jgr.game.vac.service.stereotype.GameSetting;

@Service
public class GameManagerImpl implements GameManager {
	@Autowired private GameSetupDao gameSetupDao;
	@Autowired private ApplicationContext context;
	@Autowired private PropertyDao propertyDao;
	@Autowired private GamePropsDao gamePropsDao;
	
	DateFormat dateFormat;
	private long currentGameId;
	private boolean gameActive;
	private AbstractGameObject<AbstractGameState> runningGameBean;
	private AbstractGameState gameState;
	private static ArrayList<Class<? extends BaseManagedGameBean<AbstractGameObject<AbstractGameState>>>> managedClasses = new ArrayList<>();
	private static HashMap<Class<?>, Class<?>> interfaceMappings = new HashMap<>();
	
	private class BaseManagedGameBean<T extends AbstractGameObject<AbstractGameState>> {
		GameStatus retVal;
		AbstractGameState gameState;
		T delegate;
		
		BaseManagedGameBean(T delegate, AbstractGameState gameState) {
			this.gameState = gameState;
			this.delegate = delegate;
		}

		public GameStatus getReturnValue() {
			return retVal;
		}
	}
	
	@SuppressWarnings("unused")
	private class ManagedGameOperation extends BaseManagedGameBean<AbstractGameOperation<AbstractGameState>> implements GameOperation {
		ManagedGameOperation(AbstractGameOperation<AbstractGameState> delegate, AbstractGameState gameState) {
			super(delegate, gameState);
		}
		
		@Override
		public void run() {
			retVal = delegate.run(gameState);
		}
		
		@Override
		public GameStatus runWithReturn() {
			run();
			return retVal;
		}
	}
	
	@SuppressWarnings("unused")
	private class ManagedGamePoller extends BaseManagedGameBean<AbstractGamePoller<AbstractGameState>> implements GamePoller {
		
		ManagedGamePoller(AbstractGamePoller<AbstractGameState> delegate, AbstractGameState gameState) {
			super(delegate, gameState);
		}
		
		@Override
		public long getTimeOut() {
			return delegate.getTimeOut(gameState);
		}
		
		@Override
		public GameStatus run() {
			return delegate.run(gameState);
		}
	}
	
	@SuppressWarnings("unused")
	private class ManagedGameRunnable extends BaseManagedGameBean<AbstractGameRunnable<AbstractGameState>> implements GameRunnable {
		GameStatus retVal;
		
		ManagedGameRunnable(AbstractGameRunnable<AbstractGameState> delegate, AbstractGameState gameState) {
			super(delegate, gameState);
		}
		
		@Override
		public void init() {
			delegate.init(gameState);
		}
		
		@Override
		public void run() {
			gameState.running = true;
			gameState.thread = Thread.currentThread();
			try {
				retVal = delegate.run(gameState);
			} finally {
				gameState.running = false;
				gameState.thread = null;
			}
		}
		
		@Override
		public void shutdown() {
			delegate.shutdown(gameState);
		}
		
		@Override
		public GameStatus getReturnValue() {
			return retVal;
		}
		
		@Override
		public GameStatus runWithReturn() {
			run();
			return retVal;
		}
	}
	
	@SuppressWarnings("unused")
	private class ManagedGameTestValue extends BaseManagedGameBean<AbstractGameTestValue<AbstractGameState>> implements GameTestValue {
		ManagedGameTestValue(AbstractGameTestValue<AbstractGameState> delegate, AbstractGameState gameState) {
			super(delegate, gameState);
		}
		
		@Override
		public boolean runTest() {
			return delegate.runTest(gameState);
		}
	}
	
	@SuppressWarnings("unused")
	private class ManagedGameTimedRunnable extends BaseManagedGameBean<AbstractGameTimedRunnable<AbstractGameState>> implements GameTimedRunnable {
		GameStatus retVal;
		
		ManagedGameTimedRunnable(AbstractGameTimedRunnable<AbstractGameState> delegate, AbstractGameState gameState) {
			super(delegate, gameState);
		}
		
		@Override
		public long getMaxAllowedRunTime() {
			return delegate.getMaxAllowedRunTime(gameState);
		}
		
		@Override
		public void init() {
			delegate.init(gameState);
		}
		
		
		@Override
		public void run() {
			// TODO finish this
			retVal = GameStatus.error;
			// ManagedGameRunnable timerExpired = new

			// WatchDogMaxTime maxTimer = gameState.watchDog.creatMaxTimer(delegate.getMaxAllowedRunTime(gameState), timeExpiredRunnable, delegate.getOperationName(gameState), gameState);
			try {
				retVal = delegate.run(gameState);
			} finally {
				// gameState.watchDog.removeMaxTimer(maxTimer);
			}
		}
		
		@Override
		public void shutdown() {
			delegate.shutdown(gameState);
		}
		@Override
		public GameStatus getReturnValue() {
			return retVal;
		}

		@Override
		public GameStatus runWithReturn() {
			run();
			return retVal;
		}
	}
	
	@SuppressWarnings("unused")
	private class ManagedGameThread extends BaseManagedGameBean<AbstractGameRunnable<AbstractGameState>> implements GameThread {
		GameStatus exitStatus;
		Thread thread;
		Throwable exitError;
		
		ManagedGameThread(AbstractGameRunnable<AbstractGameState> delegate,  AbstractGameState gameState) {
			super(delegate, gameState);
			thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						exitStatus = delegate.run(gameState);
					} catch(Throwable t) {
						exitError = t;
					}
				}
			});
		}
		
		@Override
		public void init() {
			delegate.init(gameState);
		}
		
		@Override
		public boolean isAlive() {
			return thread.isAlive();
		}
		
		@Override
		public void run() {
			exitStatus = delegate.run(gameState);
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void shutdown() {
			delegate.shutdown(gameState);
			thread.interrupt();
			gameState.running = false;
			// wait for a litle while for thread to exit normally then kill it
			thread.stop();
		}
		
		@Override
		public void start() {
			thread.start();
		}
		@Override
		public Throwable getExitError() {
			return exitError;
		}
		
		@Override
		public GameStatus getReturnValue() {
			return exitStatus;
		}
		
		@Override
		public GameStatus runWithReturn() {
			run();
			return retVal;
		}
	}
	
	static {
		Class<?>[] innerClasses = GameManagerImpl.class.getDeclaredClasses();
		
		for(Class<?> innerClass:innerClasses) {
			if(BaseManagedGameBean.class.isAssignableFrom(innerClass) && !innerClass.equals(BaseManagedGameBean.class)) {
				managedClasses.add(innerClass.asSubclass(BaseManagedGameBean.class.asSubclass(BaseManagedGameBean.class)));
				Class<?>[] interfaces = innerClass.getInterfaces();
				Constructor<?>[] constructors = innerClass.getDeclaredConstructors();
				if(interfaces.length != 1) {
					throw new RuntimeException("Managed class " + innerClass.getName() + " is declaring incorrect number of interfaces");
				}
				
				if(constructors.length != 1) {
					throw new RuntimeException("Managed class " + innerClass.getName() + " is declaring incorrect number of constructors");
				}
				if(constructors[0].getParameterCount() != 3) {
					throw new RuntimeException("Managed class " + innerClass.getName() + " constructor does not declare the correct number of paramaters");
				}
				interfaceMappings.put(interfaces[0], constructors[0].getParameterTypes()[1]);
			}
		}
	}
	
	public GameManagerImpl() {
		dateFormat = new SimpleDateFormat();
		
	}
	
	public static Class<?> getMappedClass(Class<?> theClass) {
		return interfaceMappings.get(theClass);
	}
	
	@Override
	public void getNeededGameProps(long gameId) {
		// TODO Auto-generated method stub
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void runGame(long gameId) throws Exception {
		this.currentGameId = gameId;
		Optional<GameSetup> daoReturn = gameSetupDao.findById(gameId);
		GameSetup gameSetup = daoReturn.get();
		
		String name = gameSetup.getBeanName();
		
		runningGameBean = (AbstractGameObject<AbstractGameState>) context.getBean(name);
		gameState = createStateFor(runningGameBean);
		
	}
	
	private AbstractGameState createStateFor(AbstractGameObject<AbstractGameState> gameObject) throws Exception {
		Class<AbstractGameState> gameStateClass = gameObject.getStateClass();
		AbstractGameState gameState = gameStateClass.getConstructor().newInstance();

		Field[] classFields = gameStateClass.getDeclaredFields();
		for(Field field:classFields) {
			GameSetting gameSetting = field.getAnnotation(GameSetting.class);
			if(gameSetting != null) {
				Property prop = propertyDao.getByName(gameSetting.name());
				GameProps gameProp = gamePropsDao.findByGameIdAndPropId(currentGameId, prop.getId());
				String propValue;
				if(gameProp == null) {
					propValue = prop.getInitalValue();
				} else {
					propValue = gameProp.getValue();
				}
				
				Class<?> fieldType = field.getType();
				if(String.class.equals(fieldType)) {
					field.set(gameState, propValue);
				} else if(Integer.class.equals(fieldType) || int.class.equals(fieldType)) {
					field.set(gameState, Integer.parseInt(propValue));
				} else if(Long.class.equals(fieldType) || long.class.equals(fieldType)) {
					field.set(gameState, Long.parseLong(propValue));
				} else if(Float.class.equals(fieldType) || float.class.equals(fieldType)) {
					field.set(gameSetting, Float.parseFloat(propValue));
				} else if(Double.class.equals(fieldType) || double.class.equals(fieldType)) {
					field.set(gameState, Double.parseDouble(propValue));
				} else if(Boolean.class.equals(fieldType) || boolean.class.equals(fieldType)) {
					field.set(gameState, Boolean.parseBoolean(propValue));
				} else if(Date.class.isAssignableFrom(fieldType)) {
					field.set(gameState, dateFormat.parse(propValue));
				} else if(Calendar.class.isAssignableFrom(fieldType)) {
					Date date = dateFormat.parse(propValue);
					Calendar calendar = new GregorianCalendar();
					calendar.setTime(date);
					field.set(gameState, calendar);
				} else { 
					// is this a managed type
					processManagedBean(gameState, field, propValue, fieldType);
				}
			}
		}
		
		return gameState;
	}

	public void processManagedBean(AbstractGameState gameState, Field field, String propValue, Class<?> fieldType) throws NoSuchMethodException, Exception, IllegalAccessException, InstantiationException, InvocationTargetException {
		boolean fieldSet = false;
		
		for(Class<?> managedClass:managedClasses) {
			if(fieldSet) break;
			Class<?>[] managedForClasses = managedClass.getInterfaces();
			for(Class<?> managedForClass:managedForClasses) {
				if(fieldSet) break;
				if(managedForClass.equals(fieldType)) {
					Constructor<?> constructor = managedClass.getConstructor(fieldType, AbstractGameState.class);
					@SuppressWarnings("unchecked")
					AbstractGameObject<AbstractGameState> gameBean = (AbstractGameObject<AbstractGameState>) context.getBean(propValue);
					AbstractGameState gameBeanState = createStateFor(gameBean);
					field.set(gameState, constructor.newInstance(gameBean, gameBeanState));
					fieldSet = true;
				}
			}
		}
	}
	
}
