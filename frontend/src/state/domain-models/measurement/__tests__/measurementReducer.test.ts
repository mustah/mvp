import {normalize} from 'normalizr';
import {EndPoints, HttpMethod} from '../../domainModels';
import {NormalizedPaginated, NormalizedPaginatedState} from '../../paginatedDomainModels';
import {requestMethodPaginated} from '../../paginatedDomainModelsActions';
import {initialPaginatedDomain, paginatedMeasurements} from '../../paginatedDomainModelsReducer';
import {Measurement, MeasurementState} from '../measurementModels';
import {measurementSchema} from '../measurementSchema';

describe('measurementReducer', () => {
  describe('measurements, paginated', () => {

    const initialState: MeasurementState = initialPaginatedDomain<Measurement>();

    const measurementsGetRequest =
      requestMethodPaginated<NormalizedPaginated<Measurement>>(EndPoints.measurements, HttpMethod.GET);
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
    const componentId = 'list1';
    const successPayload: NormalizedPaginated<Measurement> = {
      componentId,
      ...normalize(measurement, measurementSchema),
    };

    it('has correctly normalized state', () => {
      expect(successPayload).toEqual({
        componentId,
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
      const isFetching = {...initialState};
      const stateAfterRequestInitiation = paginatedMeasurements(initialState, measurementsGetRequest.request());
      expect(stateAfterRequestInitiation).toEqual(isFetching);
    });

    it('adds new measurement to state', () => {
      const stateAfterSuccess = paginatedMeasurements(initialState, measurementsGetRequest.success(successPayload));
      const expected: NormalizedPaginatedState<Measurement> = {
        entities: {1: measurement1, 2: measurement2},
        result: {
          [componentId]: {
            content: [1, 2],
            totalPages: 1440,
            totalElements: 28800,
            last: false,
            size: 20,
            number: 0,
            first: true,
            numberOfElements: 20,
            sort: null,
            isFetching: false,
          },
        },
      };
      expect(stateAfterSuccess).toEqual(expected);
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
