create table "user_type" (
    "id"      serial primary key,
    "type"    text not null
);

create table "user" (
    "id"      serial primary key,
    "name"    text not null,
    "surname" text not null,
    "email"   text not null,
    "user_type_id" int not null references user_type(id)
);

create table "account" (
    "id"          serial primary key,
    "user_id"     int not null references "user"(id),
    "login"       text not null,
    "password"    text not null
);

create table "food_type" (
    "id"      serial primary key,
    "type"    text not null
);

create table "recipe" (
    "id"              serial primary key,
    "account_id"      int not null references account(id),
    "name"            text not null,
    "food_type_id"    int not null references food_type(id),
    "time_to_cook"    int not null,
    "process"         json not null
);

create table "plan" (
    "id"              serial primary key,
    "account_id"      int not null references account(id),
    "recipe_id"       int not null references recipe(id),
    "shopping_list"   json not null
);

create table "ingredient" (
    "id"      serial primary key,
    "weight"  int not null,
    "name"    text not null
);

create table "ingredient_recipe" (
    "id"              serial primary key,
    "recipe_id"       int not null references recipe(id),
    "ingredient_id"   int not null references ingredient(id),
    "pieces"          int not null
);