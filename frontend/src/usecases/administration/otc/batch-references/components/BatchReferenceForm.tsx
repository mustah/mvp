import Checkbox from 'material-ui/Checkbox';
import * as React from 'react';
import {ValidatorForm} from 'react-material-ui-form-validator';
import {ButtonSave} from '../../../../../components/buttons/ButtonSave';
import {ValidatedFieldInput} from '../../../../../components/inputs/ValidatedFieldInput';
import {ValidatedInputSelectable} from '../../../../../components/inputs/ValidatedInputSelectable';
import {Column} from '../../../../../components/layouts/column/Column';
import {firstUpperTranslated} from '../../../../../services/translationService';
import {BatchRequestState} from '../../../../../state/domain-models-paginated/batch-references/batchReferenceModels';
import {CallbackWith, FetchPaginated, IdNamed, uuid} from '../../../../../types/Types';
import {changeBatchReference, changeRequireApproval, selectDeviceEuis} from '../batchReferenceActions';
import {batchReferenceReducer, initialState} from '../batchReferenceReducer';

export interface StateToProps {
  devices: IdNamed[];
  isFetchingDevices: boolean;
  organisationId: uuid;
}

export interface DispatchToProps {
  fetchDevices: FetchPaginated;
  saveBatchReference: CallbackWith<BatchRequestState>;
}

type Props = StateToProps & DispatchToProps;

export const BatchReferenceForm = ({
  devices,
  fetchDevices,
  isFetchingDevices,
  organisationId,
  saveBatchReference
}: Props) => {
  const [state, dispatch] = React.useReducer(batchReferenceReducer, {...initialState, organisationId});
  React.useEffect(() => {
    fetchDevices(0);
  }, [isFetchingDevices]);

  const onChangeBatchReference = ev => dispatch(changeBatchReference(ev.target.value));
  const onChangeRequireApproval = ev => dispatch(changeRequireApproval(ev.target.checked));
  const onSelectDeviceEuis = (_, __, ids) => dispatch(selectDeviceEuis(ids));

  const onSubmit = (ev) => {
    ev.preventDefault();
    return saveBatchReference(state);
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

        <ValidatedInputSelectable
          disabled={devices.length === 0}
          id="deviceEuis"
          labelText={firstUpperTranslated('device eui')}
          multiple={true}
          options={devices}
          onChange={onSelectDeviceEuis}
          value={state.deviceEuis}
        />

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
