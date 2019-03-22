import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {
  collectionStatClearError,
  fetchCollectionStats
} from '../../../state/domain-models/collection-stat/collectionStatActions';
import {
  CollectionStat, CollectionStatParameters,
  FetchCollectionStats
} from '../../../state/domain-models/collection-stat/collectionStatModels';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {getError} from '../../../state/domain-models/domainModelsSelectors';
import {
  exportToExcelSuccess,
} from '../../../state/ui/graph/measurement/measurementActions';
import {
  getCollectionStatParameters,
  getUserSelectionId
} from '../../../state/user-selection/userSelectionSelectors';
import {Callback, CallbackWith, EncodedUriParameters, ErrorResponse, OnClick, uuid} from '../../../types/Types';
import {getCollectionStatRequestParameters} from '../collectionSelectors';
import {CollectionStatBarChart} from '../components/CollectionStatBarChart';
import {addAllToReport} from '../../report/reportActions';
import {LegendItem} from '../../report/reportModels';

export interface StateToProps {
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
  parameters: EncodedUriParameters;
  requestParameters: CollectionStatParameters;
  collectionStats: ObjectsById<CollectionStat>;
  userSelectionId: uuid;
}

export interface DispatchToProps {
  clearError: OnClick;
  fetchCollectionStats: FetchCollectionStats;
  addAllToReport: CallbackWith<LegendItem[]>;
  exportToExcelSuccess: Callback;
}

const mapStateToProps = (rootState: RootState): StateToProps => {
  const {domainModels: {collectionStats}, userSelection: {userSelection}} = rootState;
  return ({
    error: getError(collectionStats),
    isFetching: collectionStats.isFetching,
    collectionStats: collectionStats.entities,
    parameters: getCollectionStatParameters({userSelection}),
    requestParameters: getCollectionStatRequestParameters(rootState),
    userSelectionId: getUserSelectionId(rootState.userSelection),
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  clearError: collectionStatClearError,
  fetchCollectionStats,
  addAllToReport,
  exportToExcelSuccess,
}, dispatch);

export const CollectionGraphContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(CollectionStatBarChart);
