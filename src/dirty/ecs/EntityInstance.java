package dirty.ecs;

import java.util.HashMap;

public class EntityInstance {
	public final int id;
	public final String entity_name;
	private HashMap<Class<? extends EntityComponent>, EntityComponent> component_map;
	private boolean enabled;
	
	public EntityInstance(int id, String entity_name) {
		this.id = id;
		this.entity_name = entity_name;
		component_map = new HashMap<Class<? extends EntityComponent>, EntityComponent>();
		enabled = true;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		if(enabled != this.enabled) {
			if(enabled) {
				Dirty.getEntity(entity_name).onEnable(this);
			}else {
				Dirty.getEntity(entity_name).onDisable(this);
			}
		}
		this.enabled = enabled;
	}
	
	public void addComponent(EntityComponent component) {
		component_map.put(component.getClass(), component);
	}
	
	public void reset() {
		for(EntityComponent component: component_map.values()) {
			component.reset();
		}
	}
	
	public <T extends EntityComponent> T getComponent(Class<T> component_type) {
		return component_type.cast(component_map.get(component_type));
	}
}