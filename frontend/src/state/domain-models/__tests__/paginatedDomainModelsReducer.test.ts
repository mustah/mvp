import {ErrorResponse, HasId} from '../../../types/Types';
import {EndPoints} from '../domainModels';
import {Measurement, MeasurementState} from '../measurement/measurementModels';
import {HasComponentId, NormalizedPaginated, NormalizedPaginatedState} from '../paginatedDomainModels';
import {requestMethodPaginated} from '../paginatedDomainModelsActions';
import {initialPaginatedDomain, paginatedMeasurements as reducer} from '../paginatedDomainModelsReducer';

describe('paginatedDomainModelsReducer', () => {

  describe('measurements, paginated', () => {

    const initialState: MeasurementState = initialPaginatedDomain<Measurement>();

    const getRequest =
      requestMethodPaginated<NormalizedPaginated<Measurement>>(EndPoints.measurements);

    const componentId = 'list1';

    const normalizedMeasurement: NormalizedPaginated<Measurement> = {
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
    };

    it('has initial state', () => {
      expect(reducer(initialState, {type: 'unknown', payload: ''})).toEqual({...initialState});
    });

    it('requests measurements', () => {
      const stateAfterRequestInitiation = reducer(initialState, getRequest.request(componentId));
      const expected = {
        ...initialState,
        result: {
          [componentId]: {isFetching: true},
        },
      };
      expect(stateAfterRequestInitiation).toEqual(expected);
    });

    it('adds new measurement to state', () => {
      const newState = reducer(initialState, getRequest.success(normalizedMeasurement));
      const expected: NormalizedPaginatedState<Measurement> = {
        entities: {...normalizedMeasurement.entities.measurements},
        result: {
          [componentId]: {
            ...normalizedMeasurement.result,
            isFetching: false,
          },
        },
      };
      expect(newState).toEqual(expected);
    });

    it('appends entities', () => {

      const populatedState: NormalizedPaginatedState<Measurement> =
        reducer(initialState, getRequest.success(normalizedMeasurement));

      const additionalComponentId = '234';

      const payload: NormalizedPaginated<HasId> = {
          componentId: additionalComponentId,
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
          [additionalComponentId]: {...payload.result, isFetching: false},
        },
      };

      const newState = reducer(populatedState, getRequest.success(payload as NormalizedPaginated<Measurement>));
      expect(newState).toEqual(expectedState);
    });

    it('has error when fetching has failed', () => {
      const payload: ErrorResponse & HasComponentId = {message: 'failed', componentId: '123'};

      const stateAfterFailure = reducer(initialState, getRequest.failure(payload));

      const failedState = {
        ...initialState,
        result: {
          123: {
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
