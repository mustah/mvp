import {Measurement} from '../measurement/measurementModels';

const measurement1: Measurement = {
  id: 1,
  quantity: 'Power',
  value: 0.06368699009387613,
  unit: 'mW',
  created: 1514637786120,
  physicalMeter: {
    rel: 'self',
    href: 'http://localhost:8080/v1/api/physical-meters/1',
  },
};
const measurement2: Measurement = {
  id: 2,
  quantity: 'Power',
  value: 0.24113868538294558,
  unit: 'mW',
  created: 1514638686120,
  physicalMeter: {
    rel: 'self',
    href: 'http://localhost:8080/v1/api/physical-meters/1',
  },
};
const measurement = {
  content: [
    measurement1,
    measurement2,
  ],
  totalPages: 1440,
  totalElements: 28800,
  last: false,
  size: 20,
  number: 0,
  first: true,
  numberOfElements: 20,
  sort: null,
};
