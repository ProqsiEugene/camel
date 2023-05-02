-- выдает все даты больше нужной
SELECT *
FROM trains
WHERE dt_start > '2023-05-13 13:14:25';

-- выдает строку с нужным guid
SELECT *
FROM sessions
WHERE guid_session = '2ff9f679-0f79-43fb-b027-3b1e521388ba';


--находим дату по guid
select date_session
FROM sessions
where guid_session = 'b14e479b-b322-4037-ad42-c896b44610df';

--запрос из таблицы по дате
SELECT *
from trains
WHERE dt_start > '2023-06-02';

--запрос с подзапросом который выдает дату 2023-05-26
SELECT id_train, UPPER(train_name) , id_station_start, dt_start from trains
WHERE dt_start > (select date_session FROM sessions where guid_session = 'b14e479b-b322-4037-ad42-c896b44610df')
ORDER BY dt_start;

