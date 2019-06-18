import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Maybe} from '../../../helpers/Maybe';
import {encodeRequestParameters, requestParametersFrom} from '../../../helpers/urlFactory';
import {RootState} from '../../../reducers/rootReducer';
import {clearCollectionStatsError as clearError} from '../../../state/domain-models-paginated/collection-stat/collectionStatActions';
import {fetchCollectionStats} from '../../../state/domain-models/collection-stat/collectionStatActions';
import {CollectionStat} from '../../../state/domain-models/collection-stat/collectionStatModels';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {getError} from '../../../state/domain-models/domainModelsSelectors';
import {EncodedUriParameters, ErrorResponse, Fetch, OnClick} from '../../../types/Types';
import {getCollectionStatRequestParameters} from '../collectionSelectors';
import {CollectionStatBarChart} from '../components/CollectionStatBarChart';

export interface StateToProps {
  collectionStats: ObjectsById<CollectionStat>;
  error: Maybe<ErrorResponse>;
  isFetching: boolean;
  parameters: EncodedUriParameters;
}

export interface DispatchToProps {
  clearError: OnClick;
  fetchCollectionStats: Fetch;
}

const mapStateToProps = (rootState: RootState): StateToProps => {
  const {domainModels: {collectionStats}} = rootState;

  return ({
    collectionStats: collectionStats.entities,
    error: getError(collectionStats),
    isFetching: collectionStats.isFetching,
    parameters: encodeRequestParameters(
      requestParametersFrom(getCollectionStatRequestParameters(rootState).selectionParameters)
    ),
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  clearError,
  fetchCollectionStats,
}, dispatch);

export const CollectionGraphContainer = connect(mapStateToProps, mapDispatchToProps)(CollectionStatBarChart);
