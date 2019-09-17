import Checkbox from 'material-ui/Checkbox';
import * as React from 'react';
import {ValidatorForm} from 'react-material-ui-form-validator';
import {ButtonSave} from '../../../../../components/buttons/ButtonSave';
import {TextFieldInput} from '../../../../../components/inputs/TextFieldInput';
import {ValidatedFieldInput} from '../../../../../components/inputs/ValidatedFieldInput';
import {Column} from '../../../../../components/layouts/column/Column';
import {Row} from '../../../../../components/layouts/row/Row';
import {fromCommaSeparated, isEnter} from '../../../../../helpers/commonHelpers';
import {Maybe} from '../../../../../helpers/Maybe';
import {firstUpperTranslated} from '../../../../../services/translationService';
import {BatchRequestState} from '../../../../../state/domain-models-paginated/batch-references/batchReferenceModels';
import {CallbackWith, Omit, uuid} from '../../../../../types/Types';
import {addDeviceEuis, changeBatchReference, changeDeviceEuis, changeRequireApproval} from '../batchReferenceActions';
import {batchReferenceReducer, initialState} from '../batchReferenceReducer';
import {BatchReferenceDeviceGrid} from './BatchReferenceDeviceGrid';
import {DeviceIdsFileUploader} from './DeviceIdsFileUploader';

export interface StateToProps {
  organisationId: uuid;
  organisationShortPrefix: Maybe<string>;
}

export interface DispatchToProps {
  saveBatchReference: CallbackWith<Omit<BatchRequestState, 'deviceEuisText'>>;
}

const columnStyle: React.CSSProperties = {margin: '16px 16px 0 16px'};

type Props = StateToProps & DispatchToProps;

export const BatchReferenceForm = ({
  organisationId,
  organisationShortPrefix: shortPrefix,
  saveBatchReference
}: Props) => {
  const [state, dispatch] = React.useReducer(batchReferenceReducer, {...initialState, organisationId});

  const setDeviceEuis = (value: string) => dispatch(changeDeviceEuis(value));
  const onChangeBatchReference = (_, value) => dispatch(changeBatchReference({shortPrefix, value}));
  const onChangeDeviceEuis = (_, value) => setDeviceEuis(value);
  const onChangeRequireApproval = ev => dispatch(changeRequireApproval(ev.target.checked));
  const onKeyPress = ev => isEnter(ev) && dispatch(addDeviceEuis(fromCommaSeparated(ev.target.value)));

  const onSubmit = (ev): void => {
    ev.preventDefault();
    const formModel = {...state, deviceEuis: fromCommaSeparated(state.deviceEuisText)};
    const {deviceEuisText, canSubmitForm, ...requestModel} = formModel;
    saveBatchReference(requestModel);
  };

  return (
    <ValidatorForm onSubmit={onSubmit}>
      <Row className="flex-fill-horizontally">
        <Column style={columnStyle}>
          <ValidatedFieldInput
            autoComplete="off"
            autoFocus={true}
            hintText={shortPrefix.orElse('')}
            id="batchId"
            labelText={firstUpperTranslated('batch reference')}
            onChange={onChangeBatchReference}
            value={state.batchId}
          />

          <TextFieldInput
            autoComplete="off"
            id="deviceEuis"
            floatingLabelText={firstUpperTranslated('comma separated device euis')}
            hintText={''}
            multiLine={false}
            onChange={onChangeDeviceEuis}
            onKeyPress={onKeyPress}
            value={state.deviceEuisText}
          />

          <DeviceIdsFileUploader onLoadEnd={setDeviceEuis}/>

        </Column>

        <Column style={columnStyle}>
          <BatchReferenceDeviceGrid deviceEuis={state.deviceEuis}/>
        </Column>
      </Row>

      <Column style={columnStyle}>
        <Checkbox
          checked={state.requireApproval}
          label={firstUpperTranslated('ownership request needs confirmation')}
          onClick={onChangeRequireApproval}
        />

        <ButtonSave disabled={!state.canSubmitForm} style={{marginTop: 24}} type="submit"/>
      </Column>

    </ValidatorForm>
  );
};
