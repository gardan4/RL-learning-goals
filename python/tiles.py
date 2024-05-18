import numpy as np


class TileCoder:
    def __init__(self, iht_size, num_tilings, tile_width, tile_height):
        self.iht_size = iht_size
        self.num_tilings = num_tilings
        self.tile_width = tile_width
        self.tile_height = tile_height
        self.iht = IHT(iht_size)

    def get_tile_coded_state(self, state):
        position, velocity = state
        tiles_indices = tiles(self.iht, self.num_tilings,
                              [position / self.tile_width, velocity / self.tile_height])
        tile_coded_state = np.zeros(self.iht_size)
        tile_coded_state[tiles_indices] = 1
        return tile_coded_state


# Tile Coding Implementation
class IHT:
    def __init__(self, size):
        self.size = size
        self.overfullCount = 0
        self.dictionary = {}

    def getindex(self, obj, readonly=False):
        if obj in self.dictionary:
            return self.dictionary[obj]
        elif readonly:
            return None
        size = len(self.dictionary)
        if size >= self.size:
            if self.overfullCount == 0:
                print('IHT full, starting to allow collisions')
            self.overfullCount += 1
            return hash(obj) % self.size
        else:
            self.dictionary[obj] = size
            return size


def tiles(iht_or_size, num_tilings, floats, readonly=False):
    if isinstance(iht_or_size, IHT):
        iht = iht_or_size
    else:
        iht = IHT(iht_or_size)

    qfloats = [f * num_tilings for f in floats]
    coordinates = [int(f) for f in qfloats]
    tiles = []
    for tiling in range(num_tilings):
        tiling_tiles = iht.getindex(tuple(coordinates), readonly)
        tiles.append(tiling_tiles)
        coordinates = [coordinates[i] + 1 for i in range(len(coordinates))]
    return tiles
