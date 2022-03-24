insert into user
    (username, password, role, enabled)
values
       ('admin', '$2a$10$yVEw7X064WXb9T/bJ/LD2eGbG7Ue8cRQgUXxFL5ajh.xh2zM6QAda', 'ADMIN', true),
       #just for testing purposes, some teacher features may not work
#        ('teacher', '$2a$10$UeVtJU.C2X6ygb0RS2JlpejHXyMB4yhmvt.0B.Qu66j2o0e3jDnD.', 'TEACHER'),
       #just for testing purposes, some student features may not work
#        ('student', '$2a$10$sVpSAZl0Yyo5YWi/5oYwdORqwsE8rxPHJ.xu1LcM/J/uBwPDdbgeC', 'STUDENT'),
       ('darrellbowen81', '$2a$10$UjB1LNZg1z/aV2m.aMD5/OMo1PNiQbaTIoW/f3vhqnyRnh2N5tucW', 'TEACHER', true),
       ('lilianstafford67', '$2a$10$hCohhYKDawvKjhiUzJuqJOfbtfOS8WCphirBUsgpMVIXxQVkIulS6', 'TEACHER', true),
       ('johndoe91', '$2a$10$0Uq8bcLPXpPE76quhjJq0Oz.iluSIBRKtZHU0pt0EwCLNNOyC5nYC', 'STUDENT', true),
       ('janedoe63', '$2a$10$K5vPvVZu/ZG/1bBUoSXnc.pr9cKXbBHfpCGWivPkoEm08WllxRvX.', 'STUDENT', true)