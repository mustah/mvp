import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {Bold} from '../../common/components/texts/Texts';
import {ValidationState} from '../../validation/models/Validation';
import {fetchDataAnalysis} from '../dataAnalysisActions';

export interface DataAnalysisContainerProps {
  fetchDataAnalysis: () => any;
  dataAnalysis: ValidationState;
}

const DataAnalysisContainer = (props: DataAnalysisContainerProps) => {
  const {title} = props.dataAnalysis;
  return (
    <div>
      <Bold>{title}</Bold>
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
