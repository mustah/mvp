import * as React from 'react';
import {Provider} from 'react-redux';
import {store} from '../../store/configureStore';
import {WithChildren} from '../../types/Types';

/**
 * Mainly used as child component of material ui's popover content. This has to be wrapped again since the provided
 * context.store is removed when child components are rendered. So the store needs to be wrapped again when
 * renderPopoverContent function is called.
 *
 * This component can be used outside the renderPopoverContent too.
 */
export const StoreProvider = ({children}: WithChildren) => (
  <Provider store={store}>
    {children}
  </Provider>
);
