import Checkbox from 'material-ui/Checkbox';
import React from 'react';
import {ValidatorForm} from 'react-material-ui-form-validator';
import {firstUpperTranslated} from '../../services/translationService';
import {
  Medium,
  MeterDefinition,
  MeterDefinitionMaybeId,
  Quantity,
} from '../../state/domain-models/meter-definitions/meterDefinitionModels';
import {noOrganisation, Organisation} from '../../state/domain-models/organisation/organisationModels';
import {CallbackWithData, uuid} from '../../types/Types';
import {QuantityList} from '../../usecases/administration/components/QuantityList';
import {ButtonSave} from '../buttons/ButtonSave';
import {ValidatedFieldInput} from '../inputs/ValidatedFieldInput';
import {ValidatedInputSelectable} from '../inputs/ValidatedInputSelectable';
import {Column} from '../layouts/column/Column';
import './MeterDefinitionEditForm.scss';

const mediumById = (mediumId: number, mediums: Medium[]): Medium =>
  mediums.find(({id}) => id === mediumId)!;

const organisationById = (organisationId: uuid, organisations: Organisation[]): Organisation =>
  organisationId === noOrganisation().id
    ? noOrganisation()
    : organisations.find(({id}) => id === organisationId)!;

interface Props {
  addMeterDefinition: CallbackWithData;
  updateMeterDefinition: CallbackWithData;
  meterDef?: MeterDefinition;
  organisations: Organisation[];
  mediums: Medium[];
  allQuantities: Quantity[];
}

const initialMeterDefinition: MeterDefinitionMaybeId = {
  name: '',
  quantities: [],
  organisation: {id: '', slug: '', name: ''},
  medium: {id: '', name: ''},
  autoApply: true,
};

export const MeterDefinitionEditForm = (
  {mediums, organisations, meterDef, updateMeterDefinition, addMeterDefinition, allQuantities}: Props
) => {
  const [meterDefinition, setMeterDefinition] = React.useState<MeterDefinitionMaybeId>(
    meterDef || initialMeterDefinition
  );

  const {name, medium, organisation, autoApply, quantities} = meterDefinition;
  const nameLabel = firstUpperTranslated('name');
  const organisationLabel = firstUpperTranslated('organisation');
  const mediumLabel = firstUpperTranslated('medium');

  const setName = (event) => setMeterDefinition({...meterDefinition, name: event.target.value});
  const setAutoApply = (event) => setMeterDefinition({...meterDefinition, autoApply: event.target.checked});
  const setQuantities = (q) => setMeterDefinition({...meterDefinition, quantities: q});
  const setOrganisation = (_, __, value) => setMeterDefinition({
    ...meterDefinition,
    organisation: organisationById(value, organisations)
  });
  const setMedium = (_, __, value) => setMeterDefinition({
    ...meterDefinition,
    medium: mediumById(value, mediums)
  });

  const wrappedSubmit = (event) => {
    event.preventDefault();

    if (meterDefinition.id) {
      updateMeterDefinition(meterDefinition);
    } else {
      addMeterDefinition(meterDefinition);
    }
  };

  const organisationId: uuid = organisation ? organisation.id : noOrganisation().id;
  const isDefault: boolean = organisationId === noOrganisation().id;

  const requiredValidator: string[] = ['required'];
  const requiredMessage: string[] = [firstUpperTranslated('required field')];
  return (
    <ValidatorForm style={{flex: 1}} onSubmit={wrappedSubmit}>
      <Column className="EditMeterDefinitionContainer">
        <ValidatedFieldInput
          autoComplete="off"
          floatingLabelText={nameLabel}
          hintText={nameLabel}
          id="name"
          value={name}
          disabled={isDefault}
          onChange={setName}
          validators={requiredValidator}
          errorMessages={requiredMessage}
        />
        <ValidatedInputSelectable
          options={mediums}
          floatingLabelText={mediumLabel}
          hintText={mediumLabel}
          id="medium"
          disabled={isDefault}
          multiple={false}
          onChange={setMedium}
          value={medium.id !== '' ? Number(medium.id) : ''}
          validators={requiredValidator}
          errorMessages={requiredMessage}
        />
        <ValidatedInputSelectable
          options={organisations}
          floatingLabelText={organisationLabel}
          hintText={organisationLabel}
          id="organisation"
          disabled={isDefault}
          multiple={false}
          onChange={setOrganisation}
          value={organisationId}
          validators={requiredValidator}
          errorMessages={requiredMessage}
        />
        <Checkbox
          label={firstUpperTranslated('default')}
          id="autoApply"
          disabled={true}
          defaultChecked={autoApply}
          onClick={setAutoApply}
          value={autoApply + ''}
        />

        <QuantityList
          changedQuantities={setQuantities}
          definitionQuantities={quantities}
          allQuantities={allQuantities}
          editable={!isDefault}
        />

        <ButtonSave
          disabled={isDefault}
          className="SaveButton"
          type="submit"
        />
      </Column>

    </ValidatorForm>
  );
};
