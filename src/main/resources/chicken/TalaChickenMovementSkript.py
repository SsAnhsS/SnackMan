#!C:\Users\derwo\AppData\Local\Programs\Python\Python311
# -*- coding: utf-8 -*-
#
#     W = Wand
#     L = Leer
#     S = Snack
#     G = Geist
#     SM = SnackMan
#
#     W W W W W -> two_North_two_West_square, two_North_one_West_square, two_North_square, two_North_one_East_square, two_North_two_East_square
#     W W W W W -> one_North_two_West_square, one_North_one_West_square, one_North_square, one_North_one_East_square, one_North_two_East_square = squares_liste
#     W W   W W -> two_West_square, one_West_square, chickens_square, one_East_square, two_East_square = squares_liste
#     W W W W W -> one_South_two_West_square, one_South_one_West_square, one_South_square, one_South_one_East_square, one_Soutn_two_East_square = squares_liste
#     W W W W W -> two_South_two_West_square, two_South_one_West_square, two_South_square, two_South_one_East_square, two_Soutn_two_East_square = squares_liste

#     direction: the direction index in which the chickens head is looking
#     e.g. if the solution liste is [W,G, ,W,2] the chicken is walking in south direction and looking into south direction too
#
#    returns: [one_north_square, one_east_square, one_south_square, one_west_square, indexOfNextPosition]

import random

def choose_next_square(squares_liste):
    """Checks if Snackman is in chickens field of view and returns solution list, with next step in direction to Snackman"""

    squares_liste = list(squares_liste)

    two_North_two_West_square, two_North_one_West_square, two_North_square, two_North_one_East_square, two_North_two_East_square = squares_liste[:5]
    one_North_two_West_square, one_North_one_West_square, one_North_square, one_North_one_East_square, one_North_two_East_square = squares_liste[5:10]
    two_West_square, one_West_square, chickens_square, one_East_square, two_East_square = squares_liste[10:15]
    one_South_two_West_square, one_South_one_West_square, one_South_square, one_South_one_East_square, one_South_two_East_square = squares_liste[15:20]
    two_South_two_West_square, two_South_one_West_square, two_South_square, two_South_one_East_square, two_South_two_East_square = squares_liste[20:25]

    solution_liste = [one_North_square, one_East_square, one_South_square, one_West_square]
    #generates solutionList with Indexes next to Chicken(because Chicken just can go one step
    newIndex = 0

    if "SM" in squares_liste:

        if ((one_North_square == "SM" or two_North_square == "SM" or two_North_one_West_square == "SM" or two_North_one_East_square == "SM" or one_North_one_West_square == "SM" or one_North_one_East_square == "SM") and one_North_square != "W"):
            return 0
        if ((one_South_square == "SM" or (two_South_square == "SM") or two_South_one_West_square == "SM" or two_South_one_East_square == "SM" or one_South_one_West_square == "SM" or one_South_one_East_square == "SM") and one_South_square != "W"):
            return 2
        if((one_West_square == "SM" or two_North_two_West_square == "SM" or one_North_two_West_square == "SM" or two_West_square == "SM" or one_South_two_West_square == "SM" or two_South_two_West_square == "SM") and one_West_square != "W"):
            return 3
        if((one_East_square == "SM" or two_North_two_East_square == "SM" or one_North_two_East_square == "SM" or two_East_square == "SM" or one_South_two_East_square == "SM" or two_South_two_East_square == "SM") and one_East_square != "W"):
            return 1
        
    elif "G" in squares_liste:
        if ((one_North_square == "G" or two_North_square == "G" or two_North_one_West_square == "G" or two_North_one_East_square == "G" or one_North_one_West_square == "G" or one_North_one_East_square == "G") and one_North_square != "W"):
            return 2
        if ((one_South_square == "G" or (two_South_square == "G") or two_South_one_West_square == "G" or two_South_one_East_square == "G" or one_South_one_West_square == "G" or one_South_one_East_square == "G") and one_South_square != "W"):
            return 0
        if((one_West_square == "G" or two_North_two_West_square == "G" or one_North_two_West_square == "G" or two_West_square == "G" or one_South_two_West_square == "G" or two_South_two_West_square == "G") and one_West_square != "W"):
            return 1
        if((one_East_square == "G" or two_North_two_East_square == "G" or one_North_two_East_square == "G" or two_East_square == "G" or one_South_two_East_square == "G" or two_South_two_East_square == "G") and one_East_square != "W"):
            return 3

    else:
        if (two_North_square == "S" and one_North_square != "W" )or one_North_square == "S":
            return 0
        if (two_East_square == "S" and one_East_square != "W") or one_East_square == "S":
            return 1
        if (two_South_square == "S" and one_South_square != "W") or one_South_square == "S":
            return 2
        if (two_West_square == "S" and one_West_square != "W") or one_West_square == "S":
            return 3

    while(solution_liste[newIndex] == "W" ):
        newIndex = random.randint(0, 3)

    return newIndex

def getWaitingTime():
    return 2000

