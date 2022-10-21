package dirty.ecs;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public abstract class EntityComponent {
	
	public void reset() {
		copy(Dirty.getComponent(getClass()));
	}
	
	public <T extends EntityComponent> void copy(T other) {
		for(Field field: other.getClass().getFields()) {
			try {
				getClass().getField(field.getName()).set(this, field.get(other));
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}
		}
	}	
	
	@SuppressWarnings("unchecked")
	public <T extends EntityComponent> T dupe() {
		Field[] fields = getClass().getFields();
		Class<?>[] field_classes = new Class<?>[fields.length];
		Object[] field_values = new Object[fields.length];
		
		try {
			for(int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				Object value = field.get(this);
				field_classes[i] = value.getClass();
				field_values[i] = value;
			}			
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}		
		try {
			return (T)getClass().getConstructor(field_classes).newInstance(field_values);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}
}