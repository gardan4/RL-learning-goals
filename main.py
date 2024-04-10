# Define the components of the MDP for the glass recycling scenario

# States are defined by the location and type of the glass
states = [
    'consumer_white', 'consumer_colored',   # Consumer has white or colored glass
    'plant_white', 'plant_colored',         # Glass at the recycling plant
    'labeler_white', 'labeler_colored',     # Glass at the labeling facility
    'consumer_recycled'                     # Recycled glass back with consumers
]

# Actions define what can be done at each step
actions = {
    'consumer_white': ['recycle_white', 'discard'],
    'consumer_colored': ['recycle_colored', 'discard'],
    'plant_white': ['process_white'],
    'plant_colored': ['process_colored'],
    'labeler_white': ['label_white'],
    'labeler_colored': ['label_colored'],
    'consumer_recycled': ['end_cycle']  # End of the cycle for recycled glass
}

# Rewards for each action
rewards = {
    'recycle_white': 5,
    'recycle_colored': 4,
    'process_white': 3,
    'process_colored': 2,
    'label_white': 1,
    'label_colored': 1,
    'discard': -10,  # Negative reward for not recycling
    'end_cycle': 0   # End of cycle, no reward or penalty
}

# Transition probabilities would normally be probabilities; for simplicity, we'll use deterministic transitions
transitions = {
    'consumer_white': {
        'recycle_white': 'plant_white',
        'discard': 'consumer_white'
    },
    'consumer_colored': {
        'recycle_colored': 'plant_colored',
        'discard': 'consumer_colored'
    },
    'plant_white': {
        'process_white': 'labeler_white'
    },
    'plant_colored': {
        'process_colored': 'labeler_colored'
    },
    'labeler_white': {
        'label_white': 'consumer_recycled'
    },
    'labeler_colored': {
        'label_colored': 'consumer_recycled'
    },
    'consumer_recycled': {
        'end_cycle': 'consumer_white'  # Assuming recycled glass goes back as white glass
    }
}

# Value function initialization - let's start with a zero value for all states
V = {state: 0 for state in states}

# Discount factor
gamma = 0.9

# Policy initialization - start with a random policy
import random

policy = {state: random.choice(actions[state]) for state in states}
print(policy)

# Show the initialized policy
def policy_evaluation(policy, V, states, transitions, rewards, gamma, theta=0.1):
    """
    Evaluate the value of a policy.
    """
    while True:
        delta = 0
        # For each state, perform a "full backup"
        for state in states:
            v = 0
            action = policy[state]
            next_state = transitions[state][action]
            # Calculate the value as the sum of immediate reward and discounted value of next state
            v = rewards[action] + gamma * V[next_state]
            # Calculate the difference between new value and old value
            delta = max(delta, abs(v - V[state]))
            V[state] = v
        # Stop evaluating once our value function change is below a small threshold
        if delta < theta:
            break
    return V

def policy_improvement(V, policy, states, actions, transitions, rewards, gamma):
    """
    Improve the policy given the value function.
    """
    policy_stable = True
    for state in states:
        old_action = policy[state]
        # Find the best action by one-step lookahead
        # Tie-breaking is done arbitrarily
        action_values = {}
        for action in actions[state]:
            next_state = transitions[state][action]
            action_values[action] = rewards[action] + gamma * V[next_state]
        best_action = max(action_values, key=action_values.get)
        # Update the policy with the best action
        policy[state] = best_action
        if old_action != best_action:
            policy_stable = False
    return policy, policy_stable

# Now let's perform policy iteration
policy_stable = False
while not policy_stable:
    V = policy_evaluation(policy, V, states, transitions, rewards, gamma)
    policy, policy_stable = policy_improvement(V, policy, states, actions, transitions, rewards, gamma)

# Show the optimal policy and state values
print(policy, V)

