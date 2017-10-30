import Dialog from 'material-ui/Dialog';
import RaisedButton from 'material-ui/RaisedButton';
import 'MeteringPoint.scss';
import * as React from 'react';
import {translate} from '../../../../../services/translationService';
import {Column} from '../../layouts/column/Column';
import {Row} from '../../layouts/row/Row';
import {Status} from '../status/Status';
import {StatusIcon} from '../status/StatusIcon';
import {Table} from '../table/Table';
import {TableHead} from '../table/TableHead';
import {TableColumn} from '../tableColumn/TableColumn';

interface MeteringPointProps {
  id: string;
}

interface MeteringPointState {
  displayDialog: boolean;
}

export class MeteringPoint extends React.Component<MeteringPointProps, MeteringPointState> {

  constructor(props) {
    super(props);

    this.state = {
      displayDialog: false,
    };
  }

  render() {
    const {id} = this.props;

    const open = (event: any): void => {
      event.preventDefault();
      this.setState({displayDialog: true});
    };

    const close = (): void => this.setState({displayDialog: false});

    const actions = [
      (
        <RaisedButton
          label={translate('close')}
          onClick={close}
        />
      ),
    ];

    const renderStatusCell = (value, index) => <Status code={value.code} content={value.text}/>;

    // TODO are these example values too large? i.e. current state, not diff between current and last state
    const meterData = {
      byId: {
        id1: {
          date: '2017-11-16 09:34',
          status: {
            code: 0,
            text: '',
          },
          quantity: 'Energy',
          value: '170.97 MWh',
          comment: '',
        },
        id2: {
          date: '2017-11-16 09:34',
          status: {
            code: 0,
            text: '',
          },
          quantity: 'Volume',
          value: '3109.81 m^3',
          comment: '',
        },
        id3: {
          date: '2017-11-16 09:34',
          status: {
            code: 0,
            text: '',
          },
          quantity: 'Power',
          value: '1.6 kW',
          comment: '',
        },
        id4: {
          date: '2017-11-16 09:34',
          status: {
            code: 0,
            text: '',
          },
          quantity: 'Volume flow',
          value: '0.029 m^3/h',
          comment: '',
        },
        id5: {
          date: '2017-11-16 09:34',
          status: {
            code: 0,
            text: '',
          },
          quantity: 'Flow temp.',
          value: '82.5 Celcius',
          comment: '',
        },
        id6: {
          date: '2017-11-16 09:34',
          status: {
            code: 3,
            text: 'Läckage',
          },
          quantity: 'Return temp.',
          value: '33.7 Celcius',
          comment: '',
        },
        id7: {
          date: '2017-11-16 09:34',
          status: {
            code: 0,
            text: '',
          },
          quantity: 'Difference temp.',
          value: '48.86 Kelvin',
          comment: '',
        },
      },
      allIds: ['id1', 'id2', 'id3', 'id4', 'id5', 'id6', 'id7'],
    };

    // TODO extract the Dialog into its own component, and keep track of its open/close state in the root.ui reducer
    return (
      <div>
        <a href={'/#/meter/' + id} onClick={open}>{id}</a>
        <Dialog
          actions={actions}
          open={this.state.displayDialog}
          onRequestClose={close}
        >
          <h2 className="capitalize">{translate('meter details')}</h2>
          <Row>
            <Column>
              <img className="MeterGraphics" src={'cme2100.jpg'}/>
            </Column>
            <Column className="OverView">
              <Row>
                <Column>
                  <Row>
                    {translate('meter id')}
                  </Row>
                  <Row>
                    12000747
                  </Row>
                </Column>
                <Column>
                  <Row>
                    {translate('product model')}
                  </Row>
                  <Row>
                    KAM
                  </Row>
                </Column>
                <Column>
                  <Row>
                    {translate('city')}
                  </Row>
                  <Row>
                    Perstorp
                  </Row>
                </Column>
                <Column>
                  <Row>
                    {translate('address')}
                  </Row>
                  <Row>
                    Duvstigen 5
                  </Row>
                </Column>
              </Row>
              <Row>
                <Column>
                  <Row>
                    {translate('medium')}
                  </Row>
                  <Row>
                    Värme, returtemp.
                  </Row>
                </Column>
                <Column>
                  <Row>
                    {translate('collection')}
                  </Row>
                  <Row>
                    <StatusIcon code={0}/>
                  </Row>
                </Column>
                <Column>
                  <Row>
                    {translate('validation')}
                  </Row>
                  <Row>
                    <StatusIcon code={3}/>
                  </Row>
                </Column>
                <Column>
                  <Row>
                    {translate('status')}
                  </Row>
                  <Row>
                    Läckage
                  </Row>
                </Column>
                <Column>
                  <Row>
                    {translate('status')}
                  </Row>
                  <Row>
                    {translate('action pending')}
                  </Row>
                </Column>
              </Row>
            </Column>
          </Row>
          <Row>
            <Table data={meterData}>
              <TableColumn
                id={'date'}
                header={<TableHead>{translate('date')}</TableHead>}
              />
              <TableColumn
                id={'status'}
                header={<TableHead>{translate('status')}</TableHead>}
                cell={renderStatusCell}
              />
              <TableColumn
                id={'quantity'}
                header={<TableHead>{translate('quantity')}</TableHead>}
              />
              <TableColumn
                id={'value'}
                header={<TableHead>{translate('value')}</TableHead>}
              />
              <TableColumn
                id={'comment'}
                header={<TableHead>{translate('comment')}</TableHead>}
              />
            </Table>
          </Row>
        </Dialog>
      </div>
    );
  }
}
