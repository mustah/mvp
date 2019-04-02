import * as React from 'react';
import {Overwrite} from 'utility-types';
import {firstUpperTranslated} from '../../services/translationService';
import {noOrganisation, Organisation} from '../../state/domain-models/organisation/organisationModels';
import {UserSelection} from '../../state/user-selection/userSelectionModels';
import {CallbackWithData, CallbackWithDataAndUrlParameters, IdNamed, uuid} from '../../types/Types';
import {ButtonSave} from '../buttons/ButtonSave';
import {SelectFieldInput} from '../inputs/InputSelectable';
import {TextFieldInput} from '../inputs/TextFieldInput';
import {Column} from '../layouts/column/Column';
import './OrganisationEditForm.scss';

const organisationById = (organisationId: uuid, organisations: Organisation[]): Organisation =>
  organisationId === noOrganisation().id
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

type State = Overwrite<Organisation, {id?: uuid}>;

export class OrganisationEditForm extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {name: '', slug: '', parent: noOrganisation(), selectionId: undefined, ...props.organisation};
  }

  componentWillReceiveProps({organisation}: Props) {
    if (organisation) {
      this.setState({...organisation});
    }
  }

  render() {
    const {parent, name, slug, selectionId} = this.state;
    const {organisations, selections} = this.props;

    const nameLabel = firstUpperTranslated('organisation name');
    const codeLabel = firstUpperTranslated('organisation slug');
    const parentLabel = firstUpperTranslated('parent organisation');
    const selectionLabel = firstUpperTranslated('selection');

    const parentId: uuid = parent ? parent.id : noOrganisation().id;

    const organisationOptions: Organisation[] = [
      noOrganisation(),
      ...organisations
        .filter((organisation: Organisation) => !organisation.parent)
    ];

    const selectionChooser =
      parent && parent.id !== noOrganisation().id
        ? (() => {
          const currentUserOwnsSelectedSelection: UserSelection | undefined = selections
            .find((selection: UserSelection) => selectionId === selection.id);

          const selectionOptions: IdNamed[] = selections
            .filter((selection: UserSelection) =>
              currentUserOwnsSelectedSelection === undefined || currentUserOwnsSelectedSelection.id === selection.id
            )
            .map(selectionOption);

          const selectedSelection: uuid | undefined = selectionId
            ? selectionId
            : selectionOptions.length && selectionOptions[0].id;

          return (
            <SelectFieldInput
              options={selectionOptions}
              floatingLabelText={selectionLabel}
              hintText={selectionLabel}
              id="selectionId"
              multiple={false}
              onChange={this.changeSelection}
              value={selectedSelection}
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
          <TextFieldInput
            autoComplete="off"
            floatingLabelText={codeLabel}
            hintText={codeLabel}
            id="slug"
            value={slug.toString()}
            onChange={this.onChangeSlug}
          />
          <SelectFieldInput
            options={organisationOptions}
            floatingLabelText={parentLabel}
            hintText={parentLabel}
            id="parent"
            multiple={false}
            onChange={this.changeParent}
            value={parentId}
          />
          {selectionChooser}
          <ButtonSave
            className="SaveButton"
            type="submit"
          />
        </Column>
      </form>
    );
  }

  changeParent = (_, __, value) => this.setState({parent: organisationById(value, this.props.organisations)});

  changeSelection = (_, __, value) => this.setState({selectionId: value});

  onChangeName = (event) => this.setState({name: event.target.value});

  onChangeSlug = (event) => this.setState({slug: event.target.value});

  wrappedSubmit = (event) => {
    event.preventDefault();

    if (this.state.id) {
      this.props.updateOrganisation(this.state);
    } else {
      const parentId: uuid | undefined = this.state.parent ? this.state.parent.id : undefined;
      const withoutParent: State = {...this.state};
      delete withoutParent.parent;

      if (parentId && parentId !== noOrganisation().id) {
        this.props.addSubOrganisation(withoutParent, parentId);
      } else {
        this.props.addOrganisation(withoutParent);
      }
    }
  }
}
