import {createSelector} from 'reselect';
import {identity} from '../../helpers/commonHelpers';
import {RootState} from '../../reducers/rootReducer';
import {CollectionStatParameters} from '../../state/domain-models/collection-stat/collectionStatModels';

export const getCollectionStatRequestParameters =
  createSelector<RootState, RootState, CollectionStatParameters>(
    identity,
    ({
      collection: {timePeriod},
      userSelection: {userSelection: {selectionParameters}},
    }) => ({
      selectionParameters: {
        ...selectionParameters,
        dateRange: timePeriod,
      },
    })
  );