import Checkbox from 'material-ui/Checkbox';
import React from 'react';
import {firstUpperTranslated} from '../../services/translationService';
import {
  Medium,
  MeterDefinition,
  MeterDefinitionMaybeId, Quantity,
} from '../../state/domain-models/meter-definitions/meterDefinitionModels';
import {noOrganisation, Organisation} from '../../state/domain-models/organisation/organisationModels';
import {CallbackWithData, uuid} from '../../types/Types';
import {QuantityList} from '../../usecases/administration/components/QuantityList';
import {ButtonSave} from '../buttons/ButtonSave';
import {SelectFieldInput} from '../inputs/InputSelectable';
import {TextFieldInput} from '../inputs/TextFieldInput';
import {Column} from '../layouts/column/Column';
import './OrganisationEditForm.scss';

const mediumById = (mediumId: uuid, mediums: Medium[]): Medium =>
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

export const initialMediumDefinition: MeterDefinitionMaybeId = {
  name: '',
  quantities: [],
  organisation: {id: '', slug: '', name: ''},
  medium: {id: '', name: ''},
  autoApply: true,
};

type state = MeterDefinitionMaybeId;

export const MeterDefinitionEditForm = (
  {mediums, organisations, meterDef, updateMeterDefinition, addMeterDefinition, allQuantities}: Props
) => {
  const [meterDefinition, setMeterDefinition] = React.useState<state>(
    meterDef ? meterDef : initialMediumDefinition
  );

  const {name, medium, organisation, autoApply, quantities} = meterDefinition;
  const nameLabel = firstUpperTranslated('name');
  const organisationLabel = firstUpperTranslated('organisation');
  const mediumLabel = firstUpperTranslated('medium');

  const setName = (event) => setMeterDefinition({...meterDefinition, name: event.target.value});
  const setAutoApply = (event) => setMeterDefinition({...meterDefinition, autoApply: event.target.checked});
  const setQuantities = (q) => setMeterDefinition({...meterDefinition, quantities: q});
  const setOrganisation = (event, index, value) => setMeterDefinition({
    ...meterDefinition,
    organisation: organisationById(value, organisations)
  });
  const setMedium = (event, index, value) => setMeterDefinition({
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

  return (
    <>
      <form style={{flex: 1}} onSubmit={wrappedSubmit}>
        <Column className="EditOrganisationContainer">
          <TextFieldInput
            autoComplete="off"
            floatingLabelText={nameLabel}
            hintText={nameLabel}
            id="name"
            value={name}
            onChange={setName}
          />
          <SelectFieldInput
            options={mediums}
            floatingLabelText={mediumLabel}
            hintText={mediumLabel}
            id="medium"
            multiple={false}
            onChange={setMedium}
            value={medium.id}
          />
          <SelectFieldInput
            options={organisations}
            floatingLabelText={organisationLabel}
            hintText={organisationLabel}
            id="organisation"
            multiple={false}
            onChange={setOrganisation}
            value={organisationId}
          />
          <Checkbox
            label={firstUpperTranslated('default')}
            id="autoApply"
            disabled={true}
            defaultChecked={autoApply}
            onClick={setAutoApply}
            value={autoApply + ''}
          />
        </Column>

        <QuantityList
          changedQuantities={setQuantities}
          definitionQuantities={quantities}
          allQuantities={allQuantities}
        />

        <ButtonSave
          className="SaveButton"
          type="submit"
        />

      </form>

    </>
  );
};
