import Checkbox from 'material-ui/Checkbox';
import * as React from 'react';
import {ValidatorForm} from 'react-material-ui-form-validator';
import {ButtonSave} from '../../../../../components/buttons/ButtonSave';
import {ValidatedFieldInput} from '../../../../../components/inputs/ValidatedFieldInput';
import {Column} from '../../../../../components/layouts/column/Column';
import {fromCommaSeparated} from '../../../../../helpers/commonHelpers';
import {firstUpperTranslated} from '../../../../../services/translationService';
import {BatchRequestState} from '../../../../../state/domain-models-paginated/batch-references/batchReferenceModels';
import {CallbackWith, Omit, uuid} from '../../../../../types/Types';
import {changeBatchReference, changeDeviceEuis, changeRequireApproval} from '../batchReferenceActions';
import {batchReferenceReducer, initialState} from '../batchReferenceReducer';
import {DeviceIdsFileUploader} from './DeviceIdsFileUploader';

export interface StateToProps {
  organisationId: uuid;
}

export interface DispatchToProps {
  saveBatchReference: CallbackWith<Omit<BatchRequestState, 'deviceEuisText'>>;
}

type Props = StateToProps & DispatchToProps;

export const BatchReferenceForm = ({organisationId, saveBatchReference}: Props) => {
  const [state, dispatch] = React.useReducer(batchReferenceReducer, {...initialState, organisationId});

  const setDeviceEuis = (result: string) => dispatch(changeDeviceEuis(result));
  const onChangeBatchReference = (_, newValue) => dispatch(changeBatchReference(newValue));
  const onChangeDeviceEuis = (_, newValue) => setDeviceEuis(newValue);
  const onChangeRequireApproval = ev => dispatch(changeRequireApproval(ev.target.checked));

  const onSubmit = (ev) => {
    ev.preventDefault();
    const requestModel = {...state, deviceEuis: fromCommaSeparated(state.deviceEuisText)};
    delete requestModel.deviceEuisText;
    return saveBatchReference(requestModel);
  };

  return (
    <ValidatorForm onSubmit={onSubmit}>
      <Column style={{margin: '16px 16px 0 16px'}}>
        <ValidatedFieldInput
          autoComplete="off"
          id="batchId"
          labelText={firstUpperTranslated('batch reference')}
          onChange={onChangeBatchReference}
          value={state.batchId}
        />

        <ValidatedFieldInput
          autoComplete="off"
          id="deviceEuis"
          labelText={firstUpperTranslated('comma separated device euis')}
          maxLength="100000"
          rowsMax={20}
          multiLine={true}
          onChange={onChangeDeviceEuis}
          value={state.deviceEuisText}
        />

        <DeviceIdsFileUploader onLoadEnd={setDeviceEuis}/>

        <Checkbox
          checked={state.requireApproval}
          label={firstUpperTranslated('ownership request needs confirmation')}
          onClick={onChangeRequireApproval}
        />

        <ButtonSave className="flex-align-self-start" style={{marginTop: 24}} type="submit"/>
      </Column>

    </ValidatorForm>
  );
};
