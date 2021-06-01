import csv

listaCoro = []
listaJohn = []

with open('gold_species_Coro.csv', 'r') as fd:
    reader = csv.reader(fd)
    for row in reader:
        listaCoro.append(row)




with open('gold_species_John.csv', 'r') as fd:
    reader = csv.reader(fd)
    for row in reader:
        listaJohn.append(row)



# for line in listaJohn:
#     conto = 0
#     for row in listaCoro:
#         if(line == row):
#             conto = 1
#     if (conto  == 0):
#         print(line)


for line in listaCoro:
    conto = 0
    for row in listaJohn:
        if(line == row):
            conto = 1
    if (conto  == 0):
        print(line)