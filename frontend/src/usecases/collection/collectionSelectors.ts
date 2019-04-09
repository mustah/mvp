import {createSelector} from 'reselect';
import {identity} from '../../helpers/commonHelpers';
import {RootState} from '../../reducers/rootReducer';
import {
  CollectionStatParameters,
} from '../../state/domain-models/collection-stat/collectionStatModels';
import {toIdNamed} from '../../types/Types';

export const getCollectionStatRequestParameters =
  createSelector<RootState, RootState, CollectionStatParameters>(
    identity,
    ({
      collection: {timePeriod},
      userSelection: {userSelection: {selectionParameters}},
      search: {validation: {query}},
    }) => ({
      selectionParameters: {
        ...selectionParameters,
        dateRange: timePeriod,
        w: query ? [toIdNamed(query)] : [],
      },
    })
  );
