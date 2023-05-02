CREATE TABLE sessions
(
    id_session   BIGSERIAL PRIMARY KEY,
    ip_session   VARCHAR(255),
    guid_session VARCHAR(255),
    time_session VARCHAR(255),
    date_session VARCHAR(255)
);

CREATE TABLE trains
(
    id_train         BIGSERIAL PRIMARY KEY,
    dt_start         VARCHAR,
    id_station_start BIGINT,
    train_name       VARCHAR(255)
);

INSERT INTO trains (dt_start, id_station_start, train_name)
SELECT to_char(now() + (n || ' days')::interval,
               'YYYY-MM-DD HH24:MI:SS'),
               n + 1,
               'Train ' || (n + 1)
FROM generate_series(0, 49) AS n;
