import {FlatButton} from 'material-ui';
import Dialog from 'material-ui/Dialog';
import 'MeteringPoint.scss';
import * as React from 'react';
import {translate} from '../../../../../services/translationService';
import {Column} from '../../layouts/column/Column';
import {Row, RowCenter} from '../../layouts/row/Row';
import {StatusIcon} from '../status/StatusIcon';

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
      this.setState((current) => ({...current, displayDialog: true}));
    };

    const close = (): void =>
      this.setState((current) => ({...current, displayDialog: false}));

    const actions = [
      (
        <FlatButton
          label={translate('close')}
          primary={true}
          onClick={close}
        />
      ),
    ];

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
            <Column>
              <Row>
                <Column>
                  <RowCenter>
                    {translate('meter id')}
                  </RowCenter>
                  <RowCenter>
                    12000747
                  </RowCenter>
                </Column>
                <Column>
                  <RowCenter>
                    {translate('product model')}
                  </RowCenter>
                  <RowCenter>
                    KAM
                  </RowCenter>
                </Column>
                <Column>
                  <RowCenter>
                    {translate('city')}
                  </RowCenter>
                  <RowCenter>
                    Perstorp
                  </RowCenter>
                </Column>
                <Column>
                  <RowCenter>
                    {translate('address')}
                  </RowCenter>
                  <RowCenter>
                    Duvstigen 5
                  </RowCenter>
                </Column>
              </Row>
              <Row>
                <Column>
                  <RowCenter>
                    {translate('collection')}
                  </RowCenter>
                  <RowCenter>
                    <StatusIcon code={0}/>
                  </RowCenter>
                </Column>
                <Column>
                  <RowCenter>
                    {translate('validation')}
                  </RowCenter>
                  <RowCenter>
                    <StatusIcon code={3}/>
                  </RowCenter>
                </Column>
                <Column>
                  <RowCenter>
                    {translate('status')}
                  </RowCenter>
                  <RowCenter>
                    Läckage
                  </RowCenter>
                </Column>
                <Column>
                  <RowCenter>
                    {translate('status')}
                  </RowCenter>
                  <RowCenter>
                    {translate('action pending')}
                  </RowCenter>
                </Column>
              </Row>
            </Column>
          </Row>
        </Dialog>
      </div>
    );
  }
}
