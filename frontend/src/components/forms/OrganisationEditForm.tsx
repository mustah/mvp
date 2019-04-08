import {omit} from 'lodash';
import * as React from 'react';
import {Overwrite} from 'utility-types';
import {firstUpperTranslated} from '../../services/translationService';
import {
  noOrganisation,
  noOrganisationId,
  Organisation
} from '../../state/domain-models/organisation/organisationModels';
import {UserSelection} from '../../state/user-selection/userSelectionModels';
import {CallbackWithData, CallbackWithDataAndUrlParameters, IdNamed, uuid} from '../../types/Types';
import {ButtonSave} from '../buttons/ButtonSave';
import {SelectFieldInput} from '../inputs/InputSelectable';
import {TextFieldInput} from '../inputs/TextFieldInput';
import {Column} from '../layouts/column/Column';
import './OrganisationEditForm.scss';

const organisationById = (organisationId: uuid, organisations: Organisation[]): Organisation =>
  organisationId === noOrganisationId
    ? noOrganisation()
    : organisations.find(({id}) => id === organisationId)!;

const selectionOption = ({id, name}: UserSelection): IdNamed => ({id, name});

interface Props {
  addOrganisation: CallbackWithData;
  addSubOrganisation: CallbackWithDataAndUrlParameters;
  organisation?: Organisation;
  organisations: Organisation[];
  selections: UserSelection[];
  selectionId?: uuid;
  updateOrganisation: CallbackWithData;
}

type State = Overwrite<Organisation, {id?: uuid, slug?: string}>;

export class OrganisationEditForm extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {name: '', selectionId: undefined, ...props.organisation};
  }

  componentWillReceiveProps({organisation}: Props) {
    if (organisation) {
      this.setState({...organisation});
    }
  }

  render() {
    const {parent, name, selectionId} = this.state;
    const {organisations, selections} = this.props;

    const nameLabel = firstUpperTranslated('organisation name');
    const parentLabel = firstUpperTranslated('parent organisation');
    const selectionLabel = firstUpperTranslated('selection');

    const parentId: uuid = parent ? parent.id : noOrganisationId;

    const parentOrganisationOptions: Organisation[] = [
      noOrganisation(),
      ...organisations
        .filter((organisation: Organisation) => !organisation.parent)
    ];

    const selectionChooser = parent && parent.id !== noOrganisationId
      ? (() => {
        const currentUserOwnsSelectedSelection: UserSelection | undefined = selections
          .find((selection: UserSelection) => selectionId === selection.id);

        const selectionOptions: IdNamed[] =
          [
            {id: -1, name: ''},
            ...selections
              .filter((selection: UserSelection) =>
                currentUserOwnsSelectedSelection === undefined || currentUserOwnsSelectedSelection.id === selection.id
              )
              .map(selectionOption)
          ];

        return (
          <SelectFieldInput
            options={selectionOptions}
            floatingLabelText={selectionLabel}
            hintText={selectionLabel}
            id="selectionId"
            multiple={false}
            onChange={this.changeSelection}
            value={selectionId || selectionOptions[0].id}
          />
        );
      })()
      : null;

    return (
      <form onSubmit={this.wrappedSubmit}>
        <Column className="EditOrganisationContainer">
          <TextFieldInput
            autoComplete="off"
            floatingLabelText={nameLabel}
            hintText={nameLabel}
            id="name"
            value={name}
            onChange={this.onChangeName}
          />
          <SelectFieldInput
            options={parentOrganisationOptions}
            floatingLabelText={parentLabel}
            hintText={parentLabel}
            id="parent"
            multiple={false}
            onChange={this.changeParent}
            value={parentId}
          />

          {selectionChooser}

          <ButtonSave className="SaveButton" type="submit"/>
        </Column>
      </form>
    );
  }

  changeParent = (_, __, value) => this.setState({parent: organisationById(value, this.props.organisations)});

  changeSelection = (_, __, value) => this.setState({selectionId: value});

  onChangeName = (event) => this.setState({name: event.target.value});

  wrappedSubmit = (event) => {
    event.preventDefault();

    const {addOrganisation, addSubOrganisation, updateOrganisation} = this.props;
    const {id, name, parent, selectionId} = this.state;
    const slug = name;
    const organisationWithoutParent = {...omit(this.state, 'parent'), slug};

    if (id) {
      updateOrganisation(parent ? {...this.state, slug} : organisationWithoutParent);
    } else {
      const parentId: uuid | undefined = parent ? parent.id : undefined;
      if (parentId && parentId !== noOrganisationId) {
        if (selectionId && selectionId !== -1) {
          addSubOrganisation(organisationWithoutParent, parentId);
        }
      } else {
        addOrganisation(organisationWithoutParent);
      }
    }
  }
}
