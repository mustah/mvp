import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Maybe} from '../../../helpers/Maybe';
import {encodeRequestParameters, requestParametersFrom} from '../../../helpers/urlFactory';
import {RootState} from '../../../reducers/rootReducer';
import {
  collectionStatClearError,
  fetchCollectionStats
} from '../../../state/domain-models/collection-stat/collectionStatActions';
import {CollectionStat, FetchCollectionStats} from '../../../state/domain-models/collection-stat/collectionStatModels';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {getError} from '../../../state/domain-models/domainModelsSelectors';
import {addAllToReport} from '../../../state/report/reportActions';
import {LegendItem} from '../../../state/report/reportModels';
import {Callback, CallbackWith, EncodedUriParameters, ErrorResponse, OnClick, Sectors} from '../../../types/Types';
import {exportToExcelSuccess} from '../collectionActions';
import {getCollectionStatRequestParameters} from '../collectionSelectors';
import {CollectionStatBarChart} from '../components/CollectionStatBarChart';

export interface StateToProps {
  collectionStats: ObjectsById<CollectionStat>;
  error: Maybe<ErrorResponse>;
  isExportingToExcel: boolean;
  isFetching: boolean;
  parameters: EncodedUriParameters;
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
  } = rootState;

  return ({
    collectionStats: collectionStats.entities,
    error: getError(collectionStats),
    isExportingToExcel,
    isFetching: collectionStats.isFetching,
    parameters: encodeRequestParameters(
      requestParametersFrom(getCollectionStatRequestParameters(rootState).selectionParameters)
    ),
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addAllToReport,
  clearError: collectionStatClearError,
  exportToExcelSuccess: exportToExcelSuccess(Sectors.collection),
  fetchCollectionStats,
}, dispatch);

export const CollectionGraphContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(CollectionStatBarChart);
