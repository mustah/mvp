import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {encodeRequestParameters, requestParametersFrom} from '../../../../helpers/urlFactory';
import {RootState} from '../../../../reducers/rootReducer';
import {
  fetchMeterCollectionStats as fetchCollectionStats,
  meterCollectionStatClearError
} from '../../../../state/domain-models/collection-stat/collectionStatActions';
import {getError} from '../../../../state/domain-models/domainModelsSelectors';
import {addAllToReport} from '../../../../state/report/reportActions';
import {Sectors, uuid} from '../../../../types/Types';
import {exportToExcelSuccess} from '../../../collection/collectionActions';
import {CollectionStatBarChart} from '../../../collection/components/CollectionStatBarChart';
import {DispatchToProps, StateToProps} from '../../../collection/containers/CollectionGraphContainer';

interface OwnProps {
  meterId: uuid;
}

const mapStateToProps = (
  {
    meterCollection: {isExportingToExcel, timePeriod},
    domainModels: {meterCollectionStats},
  }: RootState,
  {meterId}: OwnProps
): StateToProps => {

  const parameters = {...requestParametersFrom({dateRange: timePeriod}), logicalMeterId: meterId.toString()};

  return ({
    collectionStats: meterCollectionStats.entities,
    error: getError(meterCollectionStats),
    isExportingToExcel,
    isFetching: meterCollectionStats.isFetching,
    parameters: encodeRequestParameters(parameters),
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addAllToReport,
  clearError: meterCollectionStatClearError,
  exportToExcelSuccess: exportToExcelSuccess(Sectors.meterCollection),
  fetchCollectionStats,
}, dispatch);

export const CollectionGraphContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(CollectionStatBarChart);
