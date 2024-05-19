import gymnasium as gym
import numpy as np
import torch
import torch.nn as nn
import torch.optim as optim
import torch.nn.functional as F
import matplotlib.pyplot as plt
import os

# Ensure GPU is used if available
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")


# Define the policy network for continuous action space using Gaussian distribution
class PolicyNetwork(nn.Module):
    def __init__(self, input_dim, output_dim):
        super(PolicyNetwork, self).__init__()
        self.fc1 = nn.Linear(input_dim, 64)
        self.fc2 = nn.Linear(64, 24)
        self.fc3 = nn.Linear(24, output_dim)
        self.log_std = nn.Parameter(torch.zeros(output_dim))  # Learnable log standard deviation

    def forward(self, x):
        x = F.relu(self.fc1(x))
        x = F.relu(self.fc2(x))
        mean = torch.tanh(self.fc3(x))
        std = torch.exp(self.log_std)
        return mean, std

    # Saving the model
    def save_model(self, path):
        torch.save(self.state_dict(), path)

    # Loading the model
    def load_model(self, path):
        self.load_state_dict(torch.load(path))


class TileCoder:
    def __init__(self, position_range, velocity_range, num_tilings, num_tiles):
        self.position_range = position_range
        self.velocity_range = velocity_range
        self.num_tilings = num_tilings
        self.num_tiles = num_tiles

        self.tile_width = (position_range[1] - position_range[0]) / (num_tiles - 1)
        self.tile_height = (velocity_range[1] - velocity_range[0]) / (num_tiles - 1)

        self.offsets = np.linspace(0, self.tile_width * (num_tilings - 1) / num_tilings, num_tilings)

    def get_tile_indices(self, position, velocity):
        tile_indices = []
        for offset in self.offsets:
            tile_x = int((position - self.position_range[0] + offset) / self.tile_width)
            tile_y = int((velocity - self.velocity_range[0] + offset) / self.tile_height)
            tile_indices.append((tile_x % self.num_tiles, tile_y % self.num_tiles))
        return tile_indices

    def get_feature_vector(self, position, velocity):
        tile_indices = self.get_tile_indices(position, velocity)
        feature_vector = np.zeros(self.num_tilings * self.num_tiles * self.num_tiles)
        for tiling, (tile_x, tile_y) in enumerate(tile_indices):
            index = tiling * self.num_tiles * self.num_tiles + tile_y * self.num_tiles + tile_x
            feature_vector[index] = 1
        return feature_vector


# Function to play the game with the learned policy and save frames as images
def play_game(policy_net, env, num_episodes=5, output_dir="rendered_frames"):
    # Create output directory for rendered frames
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    def choose_action(state, policy_net):
        feature_vector = tile_coder.get_feature_vector(state[0], state[1])
        feature_vector = torch.tensor(feature_vector, dtype=torch.float32).unsqueeze(0).to(device)
        mean, std = policy_net(feature_vector)
        print(mean, std)
        normal = torch.distributions.Normal(mean, std)
        action = normal.sample()
        log_prob = normal.log_prob(action).sum()
        return action.detach().cpu().numpy().flatten(), log_prob

    for episode in range(num_episodes):
        state = env.reset()[0]
        total_reward = 0
        frame_idx = 0

        while True:
            action, _ = choose_action(state, policy_net)
            print(action)
            state, reward, done, _, _ = env.step(action)
            total_reward += reward

            # Render the environment and save the frame
            # frame = env.render()
            # frame_path = os.path.join(output_dir, f"episode_{episode + 1}_frame_{frame_idx}.png")
            # plt.imsave(frame_path, frame)
            # frame_idx += 1

            if done:
                break

        print(f'Episode {episode + 1}: Total Reward: {total_reward}')


env = gym.make('MountainCarContinuous-v0', render_mode="human")

position_range = env.observation_space.low[0], env.observation_space.high[0]
velocity_range = env.observation_space.low[1], env.observation_space.high[1]

# Initialize TileCoder
num_tilings = 8
num_tiles = 8
feature_vector_size = num_tilings * num_tiles * num_tiles
print(feature_vector_size)
tile_coder = TileCoder(position_range, velocity_range, num_tilings, num_tiles)

# Create the policy network
policy_net = PolicyNetwork(feature_vector_size, env.action_space.shape[0]).to(device)
policy_net.load_model('policy_net_episode_4300.pth')

# Play the game with the learned policy
play_game(policy_net, env, num_episodes=5)

env.close()
