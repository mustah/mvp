import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {SelectionOverview} from '../../common/components/selectionoverview/SelectionOverview';
import {Column} from '../../layouts/components/column/Column';
import {Layout} from '../../layouts/components/layout/Layout';
import {fetchDataAnalysis} from '../dataAnalysisActions';
import {DataAnalysisState} from '../models/DataAnalysis';

export interface DataAnalysisContainerProps {
  fetchDataAnalysis: () => any;
  dataAnalysis: DataAnalysisState;
}

const DataAnalysisContainer = (props: DataAnalysisContainerProps) => {
  const {fetchDataAnalysis} = props;
  return (
    <Layout>
      <Column className="flex-1">
        <SelectionOverview title={'Allt'}/>
        <div className="button" onClick={fetchDataAnalysis}>DATA_ANALYSIS</div>
      </Column>
    </Layout>
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
