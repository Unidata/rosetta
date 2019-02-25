/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */
package edu.ucar.unidata.rosetta.domain;

import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Style class to format toString() for pretty printing in the transaction log file.
 */
public class TransactionLogStyle extends ToStringStyle {

    public TransactionLogStyle() {
        super();
        super.setUseShortClassName(true);
        super.setUseIdentityHashCode(false);
        super.setFieldSeparator(",\n");
        super.setNullText("null");
    }

}
