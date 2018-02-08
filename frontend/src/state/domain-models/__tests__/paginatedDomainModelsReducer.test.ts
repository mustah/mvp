import {ErrorResponse, HasId} from '../../../types/Types';
import {
  HasPageNumber,
  NormalizedPaginated,
  NormalizedPaginatedState,
} from '../../domain-models-paginated/paginatedDomainModels';
import {requestMethodPaginated} from '../../domain-models-paginated/paginatedDomainModelsActions';
import {initialPaginatedDomain, measurements} from '../../domain-models-paginated/paginatedDomainModelsReducer';
import {EndPoints} from '../domainModels';
import {Measurement, MeasurementState} from '../measurement/measurementModels';

describe('paginatedDomainModelsReducer', () => {

  describe('measurements, paginated', () => {

    const initialState: MeasurementState = initialPaginatedDomain<Measurement>();

    const getRequest =
      requestMethodPaginated<NormalizedPaginated<Measurement>>(EndPoints.measurements);

    const page = 0;

    const normalizedMeasurement: NormalizedPaginated<Measurement> = {
      page,
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
    };

    it('has initial state', () => {
      expect(measurements(initialState, {type: 'unknown', payload: -1})).toEqual({...initialState});
    });

    it('requests measurements', () => {
      const stateAfterRequestInitiation = measurements(initialState, getRequest.request(page));
      const expected: NormalizedPaginatedState<Measurement> = {
        ...initialState,
        result: {
          [page]: {isFetching: true},
        },
      };
      expect(stateAfterRequestInitiation).toEqual(expected);
    });

    it('adds new measurement to state', () => {
      const newState = measurements(initialState, getRequest.success(normalizedMeasurement));
      const expected: NormalizedPaginatedState<Measurement> = {
        entities: {...normalizedMeasurement.entities.measurements},
        result: {
          [page]: {
            result: normalizedMeasurement.result.content,
            isFetching: false,
          },
        },
      };
      expect(newState).toEqual(expected);
    });

    it('appends entities', () => {

      const populatedState: NormalizedPaginatedState<Measurement> =
        measurements(initialState, getRequest.success(normalizedMeasurement));

      const anotherPage = 2;

      const payload: NormalizedPaginated<HasId> = {
          page: anotherPage,
          result: {
            content: [1, 4],
            first: true,
            last: true,
            number: 1,
            numberOfElements: 1,
            size: 1,
            sort: null,
            totalElements: 1,
            totalPages: 1,
          },
          entities: {
            measurements: {
              1: {id: 1},
              4: {id: 4},
            },
          },
        }
      ;

      const expectedState: NormalizedPaginatedState<HasId> = {
        entities: {...populatedState.entities, 1: {id: 1}, 4: {id: 4}},
        result: {
          ...populatedState.result,
          [anotherPage]: {result: payload.result.content, isFetching: false},
        },
      };

      const newState = measurements(populatedState, getRequest.success(payload as NormalizedPaginated<Measurement>));
      expect(newState).toEqual(expectedState);
    });

    it('has error when fetching has failed', () => {
      const page = 0;
      const payload: ErrorResponse & HasPageNumber = {message: 'failed', page};

      const stateAfterFailure = measurements(initialState, getRequest.failure(payload));

      const failedState = {
        ...initialState,
        result: {
          [page]: {
            error: {message: payload.message},
            isFetching: false,
          },
        },
      };
      expect(stateAfterFailure).toEqual(failedState);
    });
  });
})
;
