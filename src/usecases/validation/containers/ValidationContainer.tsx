import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {SelectionsOverview} from '../../dashboard/components/SelectionsOverview';
import {ValidationState} from '../models/Validations';
import {fetchValidations} from '../validationActions';

export interface ValidationContainerProps {
  fetchValidations: () => any;
  validation: ValidationState;
}

const ValidationContainer = (props: ValidationContainerProps) => {
  const {fetchValidations} = props;
  return (
    <div>
      <SelectionsOverview title={'Allt'}/>

      <div className="button" onClick={fetchValidations}>VALIDATIONS</div>
    </div>
  );
};

const mapStateToProps = (state: RootState) => {
  const {validation} = state;
  return {
    validation,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  fetchValidations,
}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(ValidationContainer);
