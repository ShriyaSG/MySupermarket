package discount;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry of available discount plans.
 * New plans can be registered at runtime (R5b).
 */
public class PlanRegistry {
    private final Map<String, DiscountPlan> plans = new HashMap<>();

    public PlanRegistry() {
        register(new NormalPlan());
        register(new PrimePlan());
        register(new PlatinumPlan());
    }

    public void register(DiscountPlan plan) {
        plans.put(plan.getName().toLowerCase(), plan);
    }

    public DiscountPlan get(String name) {
        return plans.get(name.toLowerCase());
    }

    public boolean contains(String name) {
        return plans.containsKey(name.toLowerCase());
    }

    public Collection<DiscountPlan> all() {
        return plans.values();
    }
}
