-- Manage office schedule here

INSERT INTO office_schedule (day_of_week, office_opens, office_closes, lunch_hour_range_start, lunch_hour_range_end)
VALUES ('MONDAY', '08:30', '17:30', '11:30', '13:30')
ON DUPLICATE KEY UPDATE
office_opens = '08:30', office_closes = '17:30', lunch_hour_range_start = '11:30', lunch_hour_range_end = '13:30';
INSERT INTO office_schedule (day_of_week, office_opens, office_closes, lunch_hour_range_start, lunch_hour_range_end)
VALUES ('TUESDAY', '08:30', '17:30', '11:30', '13:30')
ON DUPLICATE KEY UPDATE
office_opens = '08:30', office_closes = '17:30', lunch_hour_range_start = '11:30', lunch_hour_range_end = '13:30';
INSERT INTO office_schedule (day_of_week, office_opens, office_closes, lunch_hour_range_start, lunch_hour_range_end)
VALUES ('WEDNESDAY', '08:30', '17:30', '11:30', '13:30')
ON DUPLICATE KEY UPDATE
office_opens = '08:30', office_closes = '17:30', lunch_hour_range_start = '11:30', lunch_hour_range_end = '13:30';
INSERT INTO office_schedule (day_of_week, office_opens, office_closes, lunch_hour_range_start, lunch_hour_range_end)
VALUES ('THURSDAY', '08:30', '17:30', '11:30', '13:30')
ON DUPLICATE KEY UPDATE
office_opens = '08:30', office_closes = '17:30', lunch_hour_range_start = '11:30', lunch_hour_range_end = '13:30';
INSERT INTO office_schedule (day_of_week, office_opens, office_closes, lunch_hour_range_start, lunch_hour_range_end)
VALUES ('FRIDAY', '08:30', '17:30', '11:30', '13:30')
ON DUPLICATE KEY UPDATE
office_opens = '08:30', office_closes = '17:30', lunch_hour_range_start = '11:30', lunch_hour_range_end = '13:30';
INSERT INTO office_schedule (day_of_week, office_opens, office_closes, lunch_hour_range_start, lunch_hour_range_end)
VALUES ('SATURDAY', '08:30', '17:30', '00:00', '00:00')
ON DUPLICATE KEY UPDATE
office_opens = '08:30', office_closes = '17:30', lunch_hour_range_start = '00:00', lunch_hour_range_end = '00:00';

-- Add new services here
INSERT IGNORE INTO activity (name, description, concurrent) VALUES ('AGILITY', 'Obstacle course training.', false);
INSERT IGNORE INTO activity (name, description, concurrent) VALUES ('BATHING', 'Wash the dirt off.', false);
INSERT IGNORE INTO activity (name, description, concurrent) VALUES ('FEEDING', 'Fill the belly.', true);
INSERT IGNORE INTO activity (name, description, concurrent) VALUES ('GROOMING', 'Brushing, wiping, stripping fur.', false);
INSERT IGNORE INTO activity (name, description, concurrent) VALUES ('MEDICATING', 'Give pills.', false);
INSERT IGNORE INTO activity (name, description, concurrent) VALUES ('PETTING', 'Pet the pet.', false);
INSERT IGNORE INTO activity (name, description, concurrent) VALUES ('RECREATION', 'Ball, running. toys', false);
INSERT IGNORE INTO activity (name, description, concurrent) VALUES ('WALKING', 'Walkies.', true);
INSERT IGNORE INTO activity (name, description, concurrent) VALUES ('VET', 'Medical check-up.', false);

-- Add new types of pet here
INSERT IGNORE INTO pet_type (name, description, serviced) VALUES ('BIRD', 'Avian', true);
INSERT IGNORE INTO pet_type (name, description, serviced) VALUES ('CAT', 'Felis domesticus', true);
INSERT IGNORE INTO pet_type (name, description, serviced) VALUES ('DOG', 'Canis familiaris', true);
INSERT IGNORE INTO pet_type (name, description, serviced) VALUES ('FERRET', 'Mustela furo', true);
INSERT IGNORE INTO pet_type (name, description, serviced) VALUES ('FISH', 'Icthyian', true);
INSERT IGNORE INTO pet_type (name, description, serviced) VALUES ('LIZARD', 'Reptilian', true);
INSERT IGNORE INTO pet_type (name, description, serviced) VALUES ('OTHER', 'Unknown animal', true);
INSERT IGNORE INTO pet_type (name, description, serviced) VALUES ('SNAKE', 'Danger noodle', true);

-- Pet types for testing
INSERT IGNORE INTO pet_type (name, description, serviced)
VALUES ('UNICORN', 'Imaginary. Has all activities.', true);
INSERT IGNORE INTO pet_type (name, description, serviced)
VALUES ('DRAGON', 'Imaginary. Has all activities.', true);
INSERT IGNORE INTO pet_type (name, description, serviced)
VALUES ('GRYPHON', 'Imaginary. No activities.', true);
INSERT IGNORE INTO pet_type (name, description, serviced)
VALUES ('HYDRA', 'Imaginary. No activities.', true);
INSERT IGNORE INTO pet_type (name, description, serviced)
VALUES ('CHIMERA', 'Imaginary. Unserviced.', false);

-- List services by pet here
INSERT INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('BIRD', 'FEEDING', 10, 10.00, 0.00)
ON DUPLICATE KEY UPDATE
minutes = 10, cost_for_first_pet = 10.00, cost_for_additional_pet = 0.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('BIRD', 'MEDICATING', 10, 10.00, 5.00)
ON DUPLICATE KEY UPDATE
minutes = 10, cost_for_first_pet = 10.00, cost_for_additional_pet = 5.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('BIRD', 'PETTING', 15, 15.00, 10.00)
ON DUPLICATE KEY UPDATE
minutes = 15, cost_for_first_pet = 15.00, cost_for_additional_pet = 10.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('BIRD', 'RECREATION', 30, 30.00, 20.00)
ON DUPLICATE KEY UPDATE
minutes = 30, cost_for_first_pet = 30.00, cost_for_additional_pet = 20.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('BIRD', 'VET', 30, 60.00, 60.00)
ON DUPLICATE KEY UPDATE
minutes = 30, cost_for_first_pet = 60.00, cost_for_additional_pet = 60.00;

INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('CAT', 'FEEDING', 10, 10.00, 0.00)
ON DUPLICATE KEY UPDATE
minutes = 10, cost_for_first_pet = 10.00, cost_for_additional_pet = 0.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('CAT', 'GROOMING', 15, 15.00, 10.00)
ON DUPLICATE KEY UPDATE
minutes = 15, cost_for_first_pet = 15.00, cost_for_additional_pet = 10.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('CAT', 'MEDICATING', 10, 10.00, 5.00)
ON DUPLICATE KEY UPDATE
minutes = 10, cost_for_first_pet = 10.00, cost_for_additional_pet = 5.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('CAT', 'PETTING', 15, 15.00, 10.00)
ON DUPLICATE KEY UPDATE
minutes = 15, cost_for_first_pet = 15.00, cost_for_additional_pet = 10.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('CAT', 'RECREATION', 30, 30.00, 20.00)
ON DUPLICATE KEY UPDATE
minutes = 30, cost_for_first_pet = 30.00, cost_for_additional_pet = 20.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('CAT', 'VET', 30, 60.00, 60.00)
ON DUPLICATE KEY UPDATE
minutes = 30, cost_for_first_pet = 60.00, cost_for_additional_pet = 60.00;

INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('DOG', 'AGILITY', 60, 50.00, 50.00)
ON DUPLICATE KEY UPDATE
minutes = 60, cost_for_first_pet = 50.00, cost_for_additional_pet = 50.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('DOG', 'BATHING', 30, 30.00, 20.00)
ON DUPLICATE KEY UPDATE
minutes = 30, cost_for_first_pet = 30.00, cost_for_additional_pet = 20.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('DOG', 'FEEDING', 10, 10.00, 0.00)
ON DUPLICATE KEY UPDATE
minutes = 10, cost_for_first_pet = 10.00, cost_for_additional_pet = 0.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('DOG', 'GROOMING', 15, 20.00, 10.00)
ON DUPLICATE KEY UPDATE
minutes = 15, cost_for_first_pet = 20.00, cost_for_additional_pet = 10.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('DOG', 'MEDICATING', 10, 10.00, 5.00)
ON DUPLICATE KEY UPDATE
minutes = 10, cost_for_first_pet = 10.00, cost_for_additional_pet = 5.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('DOG', 'PETTING', 15, 15.00, 10.00)
ON DUPLICATE KEY UPDATE
minutes = 15, cost_for_first_pet = 15.00, cost_for_additional_pet = 10.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('DOG', 'RECREATION', 60, 50.00, 40.00)
ON DUPLICATE KEY UPDATE
minutes = 60, cost_for_first_pet = 50.00, cost_for_additional_pet = 40.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('DOG', 'WALKING', 60, 20.00, 10.00)
ON DUPLICATE KEY UPDATE
minutes = 60, cost_for_first_pet = 20.00, cost_for_additional_pet = 10.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('DOG', 'VET', 30, 60.00, 60.00)
ON DUPLICATE KEY UPDATE
minutes = 30, cost_for_first_pet = 60.00, cost_for_additional_pet = 60.00;

INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('FERRET', 'BATHING', 30, 20.00, 15.00)
ON DUPLICATE KEY UPDATE
minutes = 30, cost_for_first_pet = 20.00, cost_for_additional_pet = 15.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('FERRET', 'FEEDING', 10, 10.00, 0.00)
ON DUPLICATE KEY UPDATE
minutes = 10, cost_for_first_pet = 10.00, cost_for_additional_pet = 0.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('FERRET', 'GROOMING', 15, 20.00, 10.00)
ON DUPLICATE KEY UPDATE
minutes = 15, cost_for_first_pet = 20.00, cost_for_additional_pet = 10.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('FERRET', 'MEDICATING', 10, 10.00, 5.00)
ON DUPLICATE KEY UPDATE
minutes = 10, cost_for_first_pet = 10.00, cost_for_additional_pet = 5.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('FERRET', 'RECREATION', 30, 30.00, 20.00)
ON DUPLICATE KEY UPDATE
minutes = 30, cost_for_first_pet = 30.00, cost_for_additional_pet = 20.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('FERRET', 'VET', 30, 40.00, 40.00)
ON DUPLICATE KEY UPDATE
minutes = 30, cost_for_first_pet = 40.00, cost_for_additional_pet = 40.00;

INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('FISH', 'FEEDING', 10, 10.00, 0.00)
ON DUPLICATE KEY UPDATE
minutes = 10, cost_for_first_pet = 10.00, cost_for_additional_pet = 0.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('FISH', 'MEDICATING', 5, 5.00, 5.00)
ON DUPLICATE KEY UPDATE
minutes = 5, cost_for_first_pet = 5.00, cost_for_additional_pet = 5.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('FISH', 'VET', 15, 20.00, 10.00)
ON DUPLICATE KEY UPDATE
minutes = 15, cost_for_first_pet = 20.00, cost_for_additional_pet = 10.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('LIZARD', 'FEEDING', 10, 10.00, 0.00)
ON DUPLICATE KEY UPDATE
minutes = 10, cost_for_first_pet = 10.00, cost_for_additional_pet = 0.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('LIZARD', 'MEDICATING', 10, 10.00, 5.00)
ON DUPLICATE KEY UPDATE
minutes = 10, cost_for_first_pet = 10.00, cost_for_additional_pet = 5.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('LIZARD', 'VET', 30, 50.00, 50.00)
ON DUPLICATE KEY UPDATE
minutes = 30, cost_for_first_pet = 50.00, cost_for_additional_pet = 50.00;

INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('OTHER', 'VET', 30, 60.00, 60.00)
ON DUPLICATE KEY UPDATE
minutes = 30, cost_for_first_pet = 60.00, cost_for_additional_pet = 60.00;

INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('SNAKE', 'FEEDING', 15, 15.00, 5.00)
ON DUPLICATE KEY UPDATE
minutes = 15, cost_for_first_pet = 15.00, cost_for_additional_pet = 5.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('SNAKE', 'MEDICATING', 10, 10.00, 5.00)
ON DUPLICATE KEY UPDATE
minutes = 10, cost_for_first_pet = 10.00, cost_for_additional_pet = 5.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('SNAKE', 'VET', 30, 50.00, 50.00)
ON DUPLICATE KEY UPDATE
minutes = 30, cost_for_first_pet = 50.00, cost_for_additional_pet = 50.00;

-- Test entries

INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('UNICORN', 'AGILITY', 60, 2.00, 1.00)
ON DUPLICATE KEY UPDATE
minutes = 60, cost_for_first_pet = 2.00, cost_for_additional_pet = 1.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('UNICORN', 'BATHING', 30, 2.00, 1.00)
ON DUPLICATE KEY UPDATE
minutes = 30, cost_for_first_pet = 2.00, cost_for_additional_pet = 1.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('UNICORN', 'FEEDING', 10, 2.00, 1.00)
ON DUPLICATE KEY UPDATE
minutes = 10, cost_for_first_pet = 2.00, cost_for_additional_pet = 1.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('UNICORN', 'GROOMING', 15, 2.00, 1.00)
ON DUPLICATE KEY UPDATE
minutes = 15, cost_for_first_pet = 2.00, cost_for_additional_pet = 1.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('UNICORN', 'MEDICATING', 10, 2.00, 1.00)
ON DUPLICATE KEY UPDATE
minutes = 10, cost_for_first_pet = 2.00, cost_for_additional_pet = 1.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('UNICORN', 'PETTING', 10, 2.00, 1.00)
ON DUPLICATE KEY UPDATE
minutes = 10, cost_for_first_pet = 2.00, cost_for_additional_pet = 1.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('UNICORN', 'RECREATION', 60, 2.00, 1.00)
ON DUPLICATE KEY UPDATE
minutes = 60, cost_for_first_pet = 2.00, cost_for_additional_pet = 1.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('UNICORN', 'WALKING', 10, 2.00, 1.00)
ON DUPLICATE KEY UPDATE
minutes = 10, cost_for_first_pet = 2.00, cost_for_additional_pet = 1.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('UNICORN', 'VET', 30, 2.00, 1.00)
ON DUPLICATE KEY UPDATE
minutes = 30, cost_for_first_pet = 2.00, cost_for_additional_pet = 1.00;

INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('DRAGON', 'AGILITY', 60, 2.00, 1.00)
ON DUPLICATE KEY UPDATE
minutes = 60, cost_for_first_pet = 2.00, cost_for_additional_pet = 1.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('DRAGON', 'BATHING', 30, 2.00, 1.00)
ON DUPLICATE KEY UPDATE
minutes = 30, cost_for_first_pet = 2.00, cost_for_additional_pet = 1.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('DRAGON', 'FEEDING', 10, 2.00, 1.00)
ON DUPLICATE KEY UPDATE
minutes = 10, cost_for_first_pet = 2.00, cost_for_additional_pet = 1.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('DRAGON', 'GROOMING', 15, 2.00, 1.00)
ON DUPLICATE KEY UPDATE
minutes = 15, cost_for_first_pet = 2.00, cost_for_additional_pet = 1.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('DRAGON', 'MEDICATING', 10, 2.00, 1.00)
ON DUPLICATE KEY UPDATE
minutes = 10, cost_for_first_pet = 2.00, cost_for_additional_pet = 1.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('DRAGON', 'PETTING', 10, 2.00, 1.00)
ON DUPLICATE KEY UPDATE
minutes = 10, cost_for_first_pet = 2.00, cost_for_additional_pet = 1.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('DRAGON', 'RECREATION', 60, 2.00, 1.00)
ON DUPLICATE KEY UPDATE
minutes = 60, cost_for_first_pet = 2.00, cost_for_additional_pet = 1.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('DRAGON', 'WALKING', 10, 2.00, 1.00)
ON DUPLICATE KEY UPDATE
minutes = 10, cost_for_first_pet = 2.00, cost_for_additional_pet = 1.00;
INSERT IGNORE INTO pet_activity_type (pet_type, activity, minutes, cost_for_first_pet, cost_for_additional_pet)
VALUES ('DRAGON', 'VET', 30, 2.00, 1.00)
ON DUPLICATE KEY UPDATE
minutes = 30, cost_for_first_pet = 2.00, cost_for_additional_pet = 1.00;