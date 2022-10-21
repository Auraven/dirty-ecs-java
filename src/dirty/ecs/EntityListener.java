package dirty.ecs;

public interface EntityListener {
	public void onEnable(Entity entity, EntityInstance instance);
	public void onDisable(Entity entity, EntityInstance instance);
	public void onSpawn(Entity entity, EntityInstance instance);
	public void onDespawn(Entity entity, EntityInstance instance);
}