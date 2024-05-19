import numpy as np

# Define the states and actions
states = ['s1', 's2', 's3', 's4', 's5', 's6', 's7', 's8', 's9']
actions = ['Go study', 'Go workout', 'Go party']

# Define the transition probabilities and rewards for each state and action
# Format: transitions[state][action] = [(probability, next_state, reward)]
transitions = {
    's1': {
        'Go study': [(0.8, 's4', 2), (0.2, 's1', -1)],
        'Go workout': [(0.8, 's2', 2), (0.2, 's1', -1)],
        'Go party': [(1.0, 's1', 3)]
    },
    's2': {
        'Go study': [(0.8, 's5', 2), (0.2, 's2', -1)],
        'Go workout': [(0.8, 's3', 3), (0.2, 's2', -1)],
        'Go party': [(1.0, 's1', 3)]
    },
    's3': {
        'Go study': [(0.8, 's6', 2), (0.2, 's3', -1)],
        'Go workout': [(0.8, 's3', 4), (0.2, 's2', -1)],
        'Go party': [(1.0, 's2', 3)]
    },
    's4': {
        'Go study': [(0.8, 's7', 3), (0.2, 's1', -1)],
        'Go workout': [(0.8, 's5', 2), (0.2, 's4', -1)],
        'Go party': [(1.0, 's1', 3)]
    },
    's5': {
        'Go study': [(0.8, 's8', 3), (0.2, 's2', -1)],
        'Go workout': [(0.8, 's6', 3), (0.2, 's4', -1)],
        'Go party': [(1.0, 's1', 3)]
    },
    's6': {
        'Go study': [(0.8, 's9', 3), (0.2, 's3', -1)],
        'Go workout': [(0.8, 's6', 4), (0.2, 's5', -1)],
        'Go party': [(1.0, 's2', 3)]
    },
    's7': {
        'Go study': [(0.8, 's7', 4), (0.2, 's4', -1)],
        'Go workout': [(0.8, 's8', 2), (0.2, 's7', -1)],
        'Go party': [(1.0, 's4', 3)]
    },
    's8': {
        'Go study': [(0.8, 's8', 4), (0.2, 's7', -1)],
        'Go workout': [(0.8, 's9', 3), (0.2, 's7', -1)],
        'Go party': [(1.0, 's4', 3)]
    },
    's9': {
        'Go study': [(0.8, 's9', 4), (0.2, 's6', -1)],
        'Go workout': [(0.8, 's9', 4), (0.2, 's8', -1)],
        'Go party': [(1.0, 's5', 3)]
    }
}

# Define a fixed policy
policy = {
    's1': 'Go study',
    's2': 'Go workout',
    's3': 'Go study',
    's4': 'Go study',
    's5': 'Go study',
    's6': 'Go workout',
    's7': 'Go study',
    's8': 'Go workout',
    's9': 'Go party'
}

# Initialize values for each state
V = {state: 0 for state in states}
gamma = 0.9  # discount factor
epsilon = 0.01  # convergence threshold
converged = False
iteration = 0

# Perform value iteration
while not converged:
    # print(f"Iteration {iteration + 1}")
    delta = 0
    for state in states:
        v = V[state]
        action = policy[state]
        V[state] = sum(p * (r + gamma * V[next_state]) for p, next_state, r in transitions[state][action])
        # print(f"V({state}) = {V[state]:.4f} (Action: {action}) \n")
        delta = max(delta, abs(v - V[state]))
    if delta < epsilon:
        converged = True
    iteration += 1

# Output the results
print("Final state values:")
for state in states:
    print(f"Value of {state}: {V[state]:.2f}")
