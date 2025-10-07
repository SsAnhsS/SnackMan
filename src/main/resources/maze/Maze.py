import random
import os

#returns next free(not a Wall) adjacent field
def search_free_field_adjacent(maze, x, y):
    if maze is None:
        print("Maze is none")

    if maze[x + 1][y + 1] != '#':
        return (x + 1, y + 1)
    elif maze[x][y+1] != '#':
        return (x, y+1)
    elif maze[x + 1][y] != '#':
        return (x + 1, y)
    elif maze[x - 1][y] != '#':
        return (x - 1, y)
    elif maze[x][y - 1] != '#':
        return (x,y - 1)
    
    return None

def generate_spawn_ghost(maze):
    for _ in range(5):
        random1 = random.randint(1, len(maze) - 2)
        random2 = random.randint(1, len(maze) - 2)
        if maze[random1][random2] != '#':
            maze[random1][random2] = 'G'
        else:
            result = search_free_field_adjacent(maze, random1, random2)
            if result is None:
                continue
            random1, random2 = result
            maze[random1][random2] = 'G'

    return maze

def generate_spawn_chicken(maze):
    for _ in range(10):
        random1 = random.randint(1, len(maze) - 2)
        random2 = random.randint(1, len(maze) - 2)
        if maze[random1][random2] != '#' and maze[random1][random2] != 'G' and maze[random1][random2] != 'S':
            maze[random1][random2] = 'C'
        else:
            result = search_free_field_adjacent(maze, random1, random2)
            if result is None:
                continue
            random1, random2 = result
            maze[random1][random2] = 'C'

    return maze

def generate_free_center(maze, width, height):
    schwelle = 4
    for y in range(len(maze)):
        for x in range(len(maze[y])):
            if(max(width/3, (width/2)-schwelle) < x < min(2*width/3, (width/2)+schwelle) and max(height/3, (height/2)-schwelle) < y <(min(2*height/3, (height/2)+schwelle))):
                maze[y][x] = " "
    
    return maze

def generate_spawn_snackman(maze):
    center = (int) (len(maze)/2)
    maze[center][center] = 'S'

    return maze


def generate_labyrinth(width, height):
    maze = [["#" for _ in range(width)] for _ in range(height)] 
    stack = [(1, 1)]
    maze[1][1] = " "

    while stack:
        x, y = stack[-1]
        directions = [(0, 2), (2, 0), (0, -2), (-2, 0)]
        random.shuffle(directions)

        for rx, ry in directions:
            aktx, akty = x + rx, y + ry
            if 1 <= aktx < width - 1 and 1 <= akty < height -1 and maze[akty][aktx] == "#":
                random_number = random.randint(1, 10) #snacks spawn in ratio 1:10
                if(random_number == 1):
                    maze[akty][aktx] = "o"
                    maze[y + ry//2][x + rx//2] = "o"
                else:
                    maze[akty][aktx] = " "
                    maze[y + ry//2][x + rx//2] = " "
                stack.append((aktx, akty))
                break
        else:
            stack.pop()
        
    maze = generate_spawn_ghost(maze)

    maze = generate_spawn_chicken(maze)

    maze = generate_free_center(maze, width, height)

    maze = generate_spawn_snackman(maze)

    return maze

def save_file(maze, filename="Maze.txt"):
    directory = "extensions/map"
    if not os.path.exists(directory):
        os.makedirs(directory)
    filepath = os.path.join(directory, filename)
    with open(filepath, "w") as file:
        for row in maze:
            file.write("".join(row) + "\n")

def main():
    width, height = 21, 21
    maze = generate_labyrinth(width, height)
    save_file(maze)
    
if __name__ == "__main__":
    main()