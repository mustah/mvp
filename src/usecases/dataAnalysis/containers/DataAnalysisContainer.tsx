import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {SelectionsOverview} from '../../dashboard/components/SelectionsOverview';
import {fetchDataAnalysis} from '../dataAnalysisActions';
import {DataAnalysisState} from '../models/DataAnalysis';

export interface DataAnalysisContainerProps {
  fetchDataAnalysis: () => any;
  dataAnalysis: DataAnalysisState;
}

const DataAnalysisContainer = (props: DataAnalysisContainerProps) => {
  const {fetchDataAnalysis} = props;
  return (
    <div>
      <SelectionsOverview title={'Allt'}/>

      <div className="button" onClick={fetchDataAnalysis}>DATA_ANALYSIS</div>
    </div>
  );
};

const mapStateToProps = (state: RootState) => {
  const {dataAnalysis} = state;
  return {
    dataAnalysis,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchDataAnalysis,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(DataAnalysisContainer);
