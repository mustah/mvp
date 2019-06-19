-- drop constraint first

alter table users_roles
    drop constraint users_roles_role_id_fkey;

-- role
UPDATE role
set role = 'MVP_USER'
where role = 'USER';

UPDATE role
set role = 'MVP_ADMIN'
where role = 'ADMIN';

-- users_roles

UPDATE users_roles
set role_id = 'MVP_USER'
where role_id = 'USER';

UPDATE users_roles
set role_id = 'MVP_ADMIN'
where role_id = 'ADMIN';

-- add constraint again

alter table users_roles
    add constraint users_roles_role_id_fkey
        foreign key (role_id) references role(role);



