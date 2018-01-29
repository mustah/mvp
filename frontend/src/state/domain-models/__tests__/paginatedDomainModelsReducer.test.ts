import {normalize} from 'normalizr';
import {EndPoints, HttpMethod} from '../domainModels';
import {Measurement, MeasurementState} from '../measurement/measurementModels';
import {measurementSchema} from '../measurement/measurementSchema';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {requestMethodPaginated} from '../paginatedDomainModelsActions';
import {initialPaginatedDomain, paginatedMeasurements} from '../paginatedDomainModelsReducer';

describe('paginatedDomainModelsReducer', () => {

  describe('measurements, paginated', () => {

    const initialState: MeasurementState = initialPaginatedDomain<Measurement>();

    const measurementsGetRequest =
      requestMethodPaginated<NormalizedPaginated<Measurement>>(EndPoints.measurements, HttpMethod.GET);

    const measurement = {
      content: [
        {
          id: 1,
          quantity: 'Power',
          value: 0.06368699009387613,
          unit: 'mW',
          created: 1514637786120,
          physicalMeter: {
            rel: 'self',
            href: 'http://localhost:8080/v1/api/physical-meters/1',
          },
        },
        {
          id: 2,
          quantity: 'Power',
          value: 0.24113868538294558,
          unit: 'mW',
          created: 1514638686120,
          physicalMeter: {
            rel: 'self',
            href: 'http://localhost:8080/v1/api/physical-meters/1',
          },
        },
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

    const successPayload: NormalizedPaginated<Measurement> = normalize(measurement, measurementSchema);

    it('has correctly normalized state', () => {
      expect(successPayload).toEqual({
        entities: {
          measurements: {
            1: {
              id: 1,
              quantity: 'Power',
              value: 0.06368699009387613,
              unit: 'mW',
              created: 1514637786120,
              physicalMeter: {
                rel: 'self',
                href: 'http://localhost:8080/v1/api/physical-meters/1',
              },
            },
            2: {
              id: 2,
              quantity: 'Power',
              value: 0.24113868538294558,
              unit: 'mW',
              created: 1514638686120,
              physicalMeter: {
                rel: 'self',
                href: 'http://localhost:8080/v1/api/physical-meters/1',
              },
            },
          },
        },
        result: {
          content: [1, 2],
          totalPages: 1440,
          totalElements: 28800,
          last: false,
          size: 20,
          number: 0,
          first: true,
          numberOfElements: 20,
          sort: null,
        },
      });
    });

    it('has initial state', () => {
      expect(paginatedMeasurements(initialState, {type: 'unknown'})).toEqual({...initialState});
    });

    it('requests measurements', () => {
      const isFetching = {...initialState, isFetching: true};
      const stateAfterRequestInitiation = paginatedMeasurements(initialState, measurementsGetRequest.request());
      expect(stateAfterRequestInitiation).toEqual(isFetching);
    });

    it('adds new measurement to state', () => {
      const stateAfterSuccess = paginatedMeasurements(initialState, measurementsGetRequest.success(successPayload));
      expect(stateAfterSuccess).toEqual({
        result: {
          content: [1],
          first: true,
          last: true,
          number: 0,
          numberOfElements: 1,
          size: 20,
          sort: null,
          totalElements: 1,
          totalPages: 1,
        },
        entities: {1: measurement},
        isFetching: false,
      });
    });

    it('has error when fetching has failed', () => {
      const payload = {message: 'failed'};

      const stateAfterFailure = paginatedMeasurements(initialState, measurementsGetRequest.failure(payload));

      const failedState = {
        ...initialState,
        error: payload,
      };
      expect(stateAfterFailure).toEqual(failedState);
    });
  });
});
