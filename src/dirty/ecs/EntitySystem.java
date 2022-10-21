package dirty.ecs;

import java.util.ArrayList;

public abstract class EntitySystem {
	public final Class<? extends EntityComponent>[] component_classes;
	private ArrayList<String> entity_names;
	private boolean enabled;
	
	@SafeVarargs
	public EntitySystem(boolean enabled, Class<? extends EntityComponent>... component_classes) {
		this.component_classes = component_classes;
		entity_names = new ArrayList<String>();
		this.enabled = enabled;
	}
	
	public void setEnabled(boolean enabled) {
		if(enabled != this.enabled) {
			if(enabled) {
				onEnable();
			}else {
				onDisable();
			}
		}
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void addEntityName(String name) {
		entity_names.add(name);
	}
	
	public ArrayList<String> getEntityNames(){
		return entity_names;
	}
	
	public abstract void update(Entity entity, ArrayList<EntityInstance> instances, float delta_time);
	
	public void preUpdate(float delta_time) {}
	public void postUpdate(float delta_time) {}	
	public void onEnable() {}
	public void onDisable() {}
}