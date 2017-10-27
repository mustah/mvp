import * as React from 'react';
import {Separator} from '../../../dashboard/components/separators/Separator';
import {IconCalendar} from '../icons/IconCalendar';
import {Column} from '../layouts/column/Column';
import {Row} from '../layouts/row/Row';
import {Normal} from '../texts/Texts';
import 'PeriodSelection.scss';
import {DropDownMenu, MenuItem} from 'material-ui';
import {IdNamed, uuid} from '../../../../types/Types';
import {translate} from '../../../../services/translationService';

interface Props {
  selectedPeriod?: uuid;
  periods?: IdNamed[];
}

interface State {
  selectedPeriod: IdNamed;
  periods: IdNamed[];
}

export class PeriodSelection extends React.Component<Props, State> {

  constructor(props) {
    super(props);
    this.state = {
      selectedPeriod: {id: 0, name: 'Week'},
      // TODO extract to constant and normalize, or retrieve from backend
      periods: [{id: 0, name: 'Week'}, {id: 1, name: 'Month'}, {id: 2, name: 'Quarter'}, {id: 3, name: 'Year'}],
    };
  }

  render() {
    const dropdownChanged = (event, index) => {
      this.setState({selectedPeriod: this.state.periods[index]});
    };

    return (
      <Column>
        <Normal className="uppercase">{translate('period')}</Normal>
        <Separator/>
        <Row className="Row-center">
          <IconCalendar/>
          <DropDownMenu
            maxHeight={300}
            autoWidth={false}
            className="periodSelection"
            value={this.state.selectedPeriod.id}
            onChange={dropdownChanged}
          >
            {/*TODO use periods */}
            <MenuItem value={0} label={'9 nov - 16 nov'} primaryText={translate('Last week')}/>
            <MenuItem value={1} label={'16 okt - 16 nov'} primaryText={translate('Last month')}/>
            <MenuItem value={2} label={'16 aug - 16 nov'} primaryText={translate('Last quarter')}/>
            <MenuItem value={3} label={'2016-11-16 - 2017-11-16'} primaryText={translate('Last year')}/>
          </DropDownMenu>
        </Row>
      </Column>
    );
  }
}
