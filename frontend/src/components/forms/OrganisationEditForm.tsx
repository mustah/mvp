import * as React from 'react';
import {Overwrite} from 'react-redux-typescript';
import {firstUpperTranslated} from '../../services/translationService';
import {noOrganisation, Organisation} from '../../state/domain-models/organisation/organisationModels';
import {CallbackOfData, CallbackOfDataAndUrlParameters, uuid} from '../../types/Types';
import {ButtonSave} from '../buttons/ButtonSave';
import {SelectFieldInput} from '../inputs/InputSelectable';
import {TextFieldInput} from '../inputs/InputText';
import {Column} from '../layouts/column/Column';
import './OrganisationEditForm.scss';

const organisationById = (organisationId: uuid, organisations: Organisation[]) =>
  organisationId === noOrganisation().id
    ? noOrganisation()
    : organisations.find(({id}) => id === organisationId);

interface OrganisationEditFormProps {
  addOrganisation: CallbackOfData;
  addSubOrganisation: CallbackOfDataAndUrlParameters;
  organisation?: Organisation;
  organisations: Organisation[];
}

type State = Overwrite<Organisation, {id?: uuid}>;

export class OrganisationEditForm extends React.Component<OrganisationEditFormProps, State> {

  constructor(props: OrganisationEditFormProps) {
    super(props);
    this.state = {name: '', slug: '', parent: noOrganisation(), ...props.organisation};
  }

  componentWillReceiveProps({organisation}: OrganisationEditFormProps) {
    if (organisation) {
      this.setState({...organisation});
    }
  }

  render() {
    const {parent, name, slug} = this.state;
    const {organisations} = this.props;

    const nameLabel = firstUpperTranslated('organisation name');
    const codeLabel = firstUpperTranslated('organisation slug');
    const parentLabel = firstUpperTranslated('parent organisation');

    const parentValue: uuid = parent ? parent.id : noOrganisation().id;

    const organisationOptions: Organisation[] = [noOrganisation(), ...organisations];

    return (
      <form onSubmit={this.wrappedSubmit}>
        <Column className="EditOrganisationContainer">
          <TextFieldInput
            autoComplete="off"
            floatingLabelText={nameLabel}
            hintText={nameLabel}
            id="name"
            value={name}
            onChange={this.onChange}
          />
          <TextFieldInput
            autoComplete="off"
            floatingLabelText={codeLabel}
            hintText={codeLabel}
            id="slug"
            value={slug.toString()}
            onChange={this.onChange}
          />
          <SelectFieldInput
            options={organisationOptions}
            floatingLabelText={parentLabel}
            hintText={parentLabel}
            id="parent"
            onChange={this.changeParent}
            value={parentValue}
          />
          <ButtonSave
            className="SaveButton"
            type="submit"
          />
        </Column>
      </form>
    );
  }

  changeParent = (event, index, value) =>
    this.setState({parent: organisationById(value, this.props.organisations)})

  onChange = (event) => this.setState({[event.target.id]: event.target.value});

  wrappedSubmit = (event) => {
    event.preventDefault();

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
