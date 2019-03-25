import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {
  collectionStatClearError,
  fetchCollectionStats
} from '../../../state/domain-models/collection-stat/collectionStatActions';
import {
  CollectionStat,
  CollectionStatParameters,
  FetchCollectionStats
} from '../../../state/domain-models/collection-stat/collectionStatModels';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {getError} from '../../../state/domain-models/domainModelsSelectors';
import {getCollectionStatParameters, getUserSelectionId} from '../../../state/user-selection/userSelectionSelectors';
import {Callback, CallbackWith, EncodedUriParameters, ErrorResponse, OnClick, uuid} from '../../../types/Types';
import {addAllToReport} from '../../report/reportActions';
import {LegendItem} from '../../report/reportModels';
import {exportToExcelSuccess} from '../collectionActions';
import {getCollectionStatRequestParameters} from '../collectionSelectors';
import {CollectionStatBarChart} from '../components/CollectionStatBarChart';

export interface StateToProps {
  collectionStats: ObjectsById<CollectionStat>;
  error: Maybe<ErrorResponse>;
  isExportingToExcel: boolean;
  isFetching: boolean;
  parameters: EncodedUriParameters;
  requestParameters: CollectionStatParameters;
  userSelectionId: uuid;
}

export interface DispatchToProps {
  addAllToReport: CallbackWith<LegendItem[]>;
  clearError: OnClick;
  exportToExcelSuccess: Callback;
  fetchCollectionStats: FetchCollectionStats;
}

const mapStateToProps = (rootState: RootState): StateToProps => {
  const {
    collection: {isExportingToExcel},
    domainModels: {collectionStats},
    userSelection: {userSelection}
  } = rootState;
  return ({
    collectionStats: collectionStats.entities,
    error: getError(collectionStats),
    isExportingToExcel,
    isFetching: collectionStats.isFetching,
    parameters: getCollectionStatParameters({userSelection}),
    requestParameters: getCollectionStatRequestParameters(rootState),
    userSelectionId: getUserSelectionId(rootState.userSelection),
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addAllToReport,
  clearError: collectionStatClearError,
  exportToExcelSuccess,
  fetchCollectionStats,
}, dispatch);

export const CollectionGraphContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(CollectionStatBarChart);
