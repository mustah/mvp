import Checkbox from 'material-ui/Checkbox';
import React from 'react';
import {ValidatorForm} from 'react-material-ui-form-validator';
import {ButtonSave} from '../../../../components/buttons/ButtonSave';
import {withCssStyles} from '../../../../components/hoc/withThemeProvider';
import {ValidatedFieldInput} from '../../../../components/inputs/ValidatedFieldInput';
import {ValidatedInputSelectable} from '../../../../components/inputs/ValidatedInputSelectable';
import {Column} from '../../../../components/layouts/column/Column';
import {firstUpperTranslated} from '../../../../services/translationService';
import {
  Medium,
  MeterDefinition,
  MeterDefinitionMaybeId,
  Quantity,
} from '../../../../state/domain-models/meter-definitions/meterDefinitionModels';
import {
  noOrganisation,
  noOrganisationId,
  Organisation
} from '../../../../state/domain-models/organisation/organisationModels';
import {CallbackWith, uuid} from '../../../../types/Types';
import {QuantityList} from '../../components/QuantityList';

const mediumById = (mediumId: number, mediums: Medium[]): Medium =>
  mediums.find(({id}) => id === mediumId)!;

const organisationById = (organisationId: uuid, organisations: Organisation[]): Organisation =>
  organisationId === noOrganisationId
    ? noOrganisation()
    : organisations.find(({id}) => id === organisationId)!;

interface Props {
  addMeterDefinition: CallbackWith<MeterDefinitionMaybeId>;
  updateMeterDefinition: CallbackWith<MeterDefinitionMaybeId>;
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

const ThemedQuantityList = withCssStyles(QuantityList);

export const MeterDefinitionEditForm = (
  {mediums, organisations, meterDef, updateMeterDefinition, addMeterDefinition, allQuantities}: Props
) => {
  const [meterDefinition, setMeterDefinition] = React.useState<MeterDefinitionMaybeId>(
    meterDef || initialMeterDefinition
  );

  const {name, medium, organisation, autoApply, quantities} = meterDefinition;
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

  const organisationId: uuid = organisation ? organisation.id : noOrganisationId;
  const isDefault: boolean = organisationId === noOrganisationId;

  return (
    <ValidatorForm style={{flex: 1}} onSubmit={wrappedSubmit}>
      <Column>
        <ValidatedFieldInput
          autoComplete="off"
          labelText={firstUpperTranslated('name')}
          id="name"
          value={name}
          disabled={isDefault}
          onChange={setName}
        />
        <ValidatedInputSelectable
          options={mediums}
          labelText={firstUpperTranslated('medium')}
          id="medium"
          disabled={isDefault}
          multiple={false}
          onChange={setMedium}
          value={medium.id !== '' ? Number(medium.id) : ''}
        />
        <ValidatedInputSelectable
          options={organisations}
          labelText={firstUpperTranslated('organisation')}
          id="organisation"
          disabled={isDefault}
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

        <ThemedQuantityList
          changedQuantities={setQuantities}
          definitionQuantities={quantities}
          allQuantities={allQuantities}
          editable={!isDefault}
        />

        <ButtonSave disabled={isDefault} className="flex-align-self-start" type="submit"/>
      </Column>

    </ValidatorForm>
  );
};
