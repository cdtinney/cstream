package com.cstream.notifier;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javafx.application.Platform;

public class Notifier {
	
	private static Logger LOGGER = Logger.getLogger(Notifier.class.getName());
	
	private static Notifier INSTANCE = null;

	private Map<Class<?>, Map<String, List<PropertyChangeListener>>> listeners;
	
	private Notifier() {
		listeners = new HashMap<Class<?>, Map<String, List<PropertyChangeListener>>>();
	}
	
	public static Notifier getInstance() {
		
		if (INSTANCE == null) {
			INSTANCE = new Notifier();
		}
		
		return INSTANCE;
	}
	
	public void addListener(Class<?> clazz, String property, final Object instance, final String handlerMethodName) {
		addListener(clazz, property, instance, null, null, handlerMethodName, false);				
	}

	public void addListener(Class<?> clazz, String property, final Object instance, final String handlerMethodName, boolean javaFxThread) {
		addListener(clazz, property, instance, null, null, handlerMethodName, javaFxThread);				
	}
	
	public void addListener(Class<?> clazz, String property, final Object instance, final Object data, final Class<?> dataClass, final String handlerMethodName, boolean javaFxThread) {
		
		getListeners(clazz, property).add(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				handleEvent(evt, instance, data, dataClass, handlerMethodName, javaFxThread);
			}
		
		});
		
	}
	
	public void notify(Object source, String property, Object oldValue, Object newValue) {
		
		notifyListeners(source, property, oldValue, newValue);
		
	}
	
	public void notifyListeners(Object source, String property, Object oldValue, Object newValue) {

		Class<?> clazz = source.getClass();
		
		for (PropertyChangeListener listener : getListeners(clazz, property)) {
			listener.propertyChange(new PropertyChangeEvent(source, property, oldValue, newValue));
		}
		
	}
	
	private void handleEvent(final PropertyChangeEvent evt, final Object instance, Object data, Class<?> dataClass, String handlerMethodName, boolean javaFxThread) {
		
		try {
			
			if (!javaFxThread) {
				invokeHandlerMethod(evt, instance, data, dataClass, handlerMethodName);
				
			// If we invoke a method that contains code that modifie JavaFX objects, we 
			// need to be on the JavaFX application thread (see: Platform)
			} else {
				
				Platform.runLater(() -> {
					
					try {
						invokeHandlerMethod(evt, instance, data, dataClass, handlerMethodName);
						
					} catch (Exception e) {
						e.printStackTrace();
						
					}
					
				});
			}
			
		} catch (NoSuchMethodException e) {
			LOGGER.warning("Property change handler method not found: " + instance.getClass().getSimpleName() 
			+ "." + handlerMethodName);
			
		} catch (InvocationTargetException e) {
			LOGGER.warning("Exception thrown in " + instance.getClass().getSimpleName() + "." + handlerMethodName);
			
			e.getTargetException().printStackTrace();
		
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
	}
	
	private void invokeHandlerMethod(final PropertyChangeEvent evt, final Object instance, Object data, Class<?> dataClass, String handlerMethodName) throws NoSuchMethodException, 
		SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		// No additional argument specified - handlerMethod(PropertyChangeEvent)
		if (data == null) {
			final Method handlerMethod = instance.getClass().getDeclaredMethod(handlerMethodName, evt.getClass());
			handlerMethod.setAccessible(true);	
			handlerMethod.invoke(instance, evt);
		
		// Additional argument specified - handlerMethod(dataClass)
		} else {
			Method handlerMethod = instance.getClass().getDeclaredMethod(handlerMethodName, dataClass);
			handlerMethod.setAccessible(true);	
			handlerMethod.invoke(instance, data);
			
		}
		
	}
	
	private List<PropertyChangeListener> getListeners(Class<?> clazz, String property) {
		
		// No listeners mapped to the class
		if (listeners.get(clazz) == null) {
			listeners.put(clazz, new HashMap<String, List<PropertyChangeListener>>());
		}
		
		// No listeners mapped to the property
		if (listeners.get(clazz).get(property) == null) {
			listeners.get(clazz).put(property, new ArrayList<PropertyChangeListener>());
		}
		
		return listeners.get(clazz).get(property);
		
	}

}
