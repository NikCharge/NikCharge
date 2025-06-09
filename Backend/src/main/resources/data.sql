-- ======================
-- ESTAÇÕES
-- ======================
INSERT INTO station (id, name, address, city, latitude, longitude)
VALUES
  (1, 'Estação Central', 'Rua A, 123', 'Lisboa', 38.7169, -9.1399),
  (2, 'Estação Norte', 'Avenida B, 45', 'Porto', 41.1579, -8.6291),
  (3, 'Estação Sul', 'Rua C, 789', 'Faro', 37.0194, -7.9304),
  (4, 'Estação Leste', 'Travessa D, 321', 'Coimbra', 40.2111, -8.4291),
  (5, 'Estação Oeste', 'Rua E, 654', 'Setúbal', 38.5244, -8.8882),
  (6, 'Estação Centro-Norte', 'Avenida F, 111', 'Braga', 41.5454, -8.4265);

-- ======================
-- CARREGADORES
-- ======================
INSERT INTO charger (id, station_id, charger_type, status, price_per_kwh, last_maintenance, maintenance_note)
VALUES
  -- Estação 1
  (1, 1, 'DC_FAST', 'AVAILABLE', 0.25, '2024-05-01T10:00:00', 'OK'),
  (2, 1, 'AC_STANDARD', 'IN_USE', 0.15, '2024-05-10T09:00:00', 'Uso intenso'),

  -- Estação 2
  (3, 2, 'DC_ULTRA_FAST', 'MAINTENANCE', 0.35, '2024-04-20T11:00:00', 'Aguardando peças'),
  (4, 2, 'AC_STANDARD', 'AVAILABLE', 0.10, '2024-06-01T08:00:00', 'Novo'),

  -- Estação 3
  (5, 3, 'DC_FAST', 'AVAILABLE', 0.22, '2024-05-20T14:00:00', 'Limpeza completa'),
  (6, 3, 'DC_ULTRA_FAST', 'UNDER_MAINTENANCE', 0.40, '2024-03-30T15:00:00', 'Erro de sistema'),

  -- Estação 4
  (7, 4, 'AC_STANDARD', 'AVAILABLE', 0.13, '2024-05-05T12:00:00', 'Revisado'),
  (8, 4, 'DC_FAST', 'IN_USE', 0.28, '2024-05-15T16:00:00', 'Em uso frequente'),

  -- Estação 5
  (9, 5, 'DC_ULTRA_FAST', 'AVAILABLE', 0.32, '2024-06-01T09:00:00', 'Instalado recentemente'),
  (10, 5, 'AC_STANDARD', 'MAINTENANCE', 0.12, '2024-04-25T11:30:00', 'Troca de componentes'),

  -- Estação 6
  (11, 6, 'DC_FAST', 'AVAILABLE', 0.24, '2024-05-22T10:45:00', 'OK'),
  (12, 6, 'AC_STANDARD', 'IN_USE', 0.14, '2024-05-30T13:15:00', 'Uso elevado');

-- ======================
-- DESCONTOS
-- ======================
INSERT INTO discount (id, station_id, charger_type, day_of_week, start_hour, end_hour, discount_percent, active)
VALUES
  (1, 1, 'DC_FAST', 1, 10, 14, 15.0, TRUE),
  (2, 2, 'AC_STANDARD', 5, 18, 22, 10.0, TRUE),
  (3, 3, 'DC_ULTRA_FAST', 0, 8, 12, 20.0, FALSE),
  (4, 1, 'AC_STANDARD', 6, 14, 18, 5.0, TRUE),
  (5, 4, 'DC_FAST', 2, 9, 13, 12.5, TRUE),
  (6, 5, 'DC_ULTRA_FAST', 4, 16, 20, 18.0, TRUE),
  (7, 6, 'AC_STANDARD', 3, 7, 10, 8.0, TRUE);
