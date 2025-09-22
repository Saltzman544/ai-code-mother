package com.saltzman.aicodemother.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author: Saltzman
 * @Date: 2025/09/16/12:26
 * @Description:
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    @Serial
    private static final long serialVersionUID = 1L;
}

