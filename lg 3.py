import numpy as np

# Define the states and actions
states = ['S1', 'S2', 'S3']
actions = ['A', 'B']

# Transition probabilities and rewards: {state: {action: [(prob, next_state, reward)]}}
transitions = {
    'S1': {
        'A': [(0.8, 'S1', 10), (0.2, 'S2', 0)],
        'B': [(1.0, 'S3', 5)]
    },
    'S2': {
        'A': [(0.5, 'S1', 0), (0.5, 'S3', 20)],
        'B': [(1.0, 'S2', 15)]
    },
    'S3': {
        'A': [(0.7, 'S2', 0), (0.3, 'S3', 25)],
        'B': [(1.0, 'S1', 10)]
    }
}

# Define the policy
policy = {
    'S1': 'B',
    'S2': 'B',
    'S3': 'A'
}

# Initialize values for each state
V = {state: 0 for state in states}
gamma = 0.9  # discount factor

# Function to calculate the value for a single state using the Bellman optimality equation
def calculate_value(state):
    return max(
        sum(p * (r + gamma * V[s]) for p, s, r in transitions[state][action]) for action in actions
    )

# Update the state values based on the policy until convergence
convergence = False
while not convergence:
    delta = 0
    new_V = {}
    for state in states:
        new_v = calculate_value(state)
        delta = max(delta, abs(V[state] - new_v))
        new_V[state] = new_v
    V = new_V
    if delta < 0.01:
        convergence = True

# Output the results
for state in states:
    print(f"Value of {state}: {V[state]:.2f}")
    optimal_value = calculate_value(state)
    policy_value = sum(p * (r + gamma * V[s]) for p, s, r in transitions[state][policy[state]])
    print(f"Optimal Value from Bellman: {optimal_value:.2f}, Policy Value: {policy_value:.2f}")
    assert abs(optimal_value - policy_value) < 0.01, f"Policy not optimal for state {state}"
print("The policy is optimal if no assertion errors are raised.")
